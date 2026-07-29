package com.vela.im.service.conversation.domain.service;

import com.vela.im.service.conversation.domain.entity.ImConversationSetEntity;
import com.vela.im.service.conversation.infrastructure.persistence.mapper.ImConversationSetMapper;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.application.utils.WriteUserSeq;
import com.vela.im.service.infrastructure.seq.RedisSeq;
import com.vela.im.shared.config.ImServerProperties;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.types.enums.ConversationTypeEnum;
import com.vela.im.shared.types.message.MessageReadedContent;
import com.vela.im.service.conversation.application.dto.req.DeleteConversationReq;
import com.vela.im.service.conversation.application.dto.req.UpdateConversationReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationService - 会话服务")
class ConversationServiceTest {

    @Mock private ImConversationSetMapper conversationSetMapper;
    @Mock private RedisSeq redisSeq;
    @Mock private WriteUserSeq writeUserSeq;
    @Mock private MessageProducer messageProducer;
    @Mock private ImServerProperties appConfig;

    private ConversationService service;

    @BeforeEach
    void setUp() {
        service = new ConversationService(conversationSetMapper, redisSeq,
                writeUserSeq, messageProducer, appConfig);
    }

    @Nested
    @DisplayName("messageMarkRead - 标记已读")
    class MessageMarkReadTest {
        @Test
        @DisplayName("P2P消息标记已读应更新readedSequence")
        void p2pMarkRead() {
            MessageReadedContent content = new MessageReadedContent();
            content.setConversationType(ConversationTypeEnum.P2P.getCode());
            content.setFromId("userA");
            content.setToId("userB");
            content.setAppId(100);
            content.setMessageSequence(50L);

            when(conversationSetMapper.selectOne(any())).thenReturn(null);
            when(redisSeq.doGetSeq(anyString())).thenReturn(100L);

            service.messageMarkRead(content);

            verify(conversationSetMapper, times(1)).insert(any(ImConversationSetEntity.class));
            verify(writeUserSeq, times(1)).writeUserSeq(anyInt(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("会话已存在时应更新readedSequence")
        void existingConversationUpdatesReadedSeq() {
            MessageReadedContent content = new MessageReadedContent();
            content.setConversationType(ConversationTypeEnum.P2P.getCode());
            content.setFromId("userA"); content.setToId("userB");
            content.setAppId(100); content.setMessageSequence(50L);

            when(conversationSetMapper.selectOne(any())).thenReturn(new ImConversationSetEntity());
            when(redisSeq.doGetSeq(anyString())).thenReturn(100L);

            service.messageMarkRead(content);

            verify(conversationSetMapper, times(1)).readMark(any(ImConversationSetEntity.class));
        }
    }

    @Nested
    @DisplayName("deleteConversation - 删除会话")
    class DeleteConversationTest {
        @Test
        @DisplayName("同步模式为1时应通知其他端")
        void syncModeNotifiesOtherDevices() {
            DeleteConversationReq req = new DeleteConversationReq();
            req.setConversationId("conv123");
            req.setFromId("userA");
            req.setAppId(100);

            when(appConfig.getDeleteConversationSyncMode()).thenReturn(1);

            Result result = service.deleteConversation(req);

            assertTrue(result.isOk());
            verify(messageProducer).sendToUserExceptClient(anyString(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("updateConversation - 更新会话设置")
    class UpdateConversationTest {
        @Test
        @DisplayName("未设置任何参数应返回错误")
        void noParamsReturnsError() {
            UpdateConversationReq req = new UpdateConversationReq();

            Result result = service.updateConversation(req);

            assertFalse(result.isOk());
        }

        @Test
        @DisplayName("设置置顶应更新DB并同步")
        void setTopUpdatesDB() {
            UpdateConversationReq req = new UpdateConversationReq();
            req.setConversationId("conv123");
            req.setAppId(100);
            req.setFromId("userA");
            req.setIsTop(1);

            when(conversationSetMapper.selectOne(any())).thenReturn(new ImConversationSetEntity());
            when(redisSeq.doGetSeq(anyString())).thenReturn(100L);

            Result result = service.updateConversation(req);

            assertTrue(result.isOk());
            verify(conversationSetMapper).update(any(), any());
            verify(messageProducer).sendToUserExceptClient(anyString(), any(), any(), any());
        }
    }
}
