package com.vela.im.service.friendship.infrastructure.service;

import com.vela.im.service.application.utils.CallbackService;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.application.utils.WriteUserSeq;
import com.vela.im.service.friendship.domain.service.ImFriendShipRequestService;
import com.vela.im.service.friendship.infrastructure.persistence.mapper.ImFriendShipMapper;
import com.vela.im.service.infrastructure.seq.RedisSeq;
import com.vela.im.service.user.domain.service.ImUserService;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.config.ImServerProperties;
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
@DisplayName("ImFriendServiceImpl - 好友服务")
class ImFriendServiceImplTest {

    @Mock private ImFriendShipMapper friendShipMapper;
    @Mock private ImUserService imUserService;
    @Mock private ImServerProperties appConfig;
    @Mock private CallbackService callbackService;
    @Mock private MessageProducer messageProducer;
    @Mock private ImFriendShipRequestService friendShipRequestService;
    @Mock private RedisSeq redisSeq;
    @Mock private WriteUserSeq writeUserSeq;

    private ImFriendServiceImpl friendService;

    @BeforeEach
    void setUp() {
        friendService = new ImFriendServiceImpl(friendShipMapper, imUserService, appConfig,
                callbackService, messageProducer, null, friendShipRequestService, redisSeq, writeUserSeq);
    }

    @Nested
    @DisplayName("getAllFriendShip - 获取好友列表")
    class GetAllFriendShipTest {
        @Test
        @DisplayName("应返回好友列表")
        void returnsFriendList() {
            var req = new com.vela.im.service.friendship.application.dto.req.GetAllFriendShipReq();
            req.setAppId(100);
            req.setFromId("user001");

            Result result = friendService.getAllFriendShip(req);

            assertTrue(result.isOk());
            verify(friendShipMapper).selectList(any());
        }
    }

    @Nested
    @DisplayName("getRelation - 获取关系")
    class GetRelationTest {
        @Test
        @DisplayName("不存在的关系应返回错误")
        void nonExistent() {
            when(friendShipMapper.selectOne(any())).thenReturn(null);
            var req = new com.vela.im.service.friendship.application.dto.req.GetRelationReq();
            req.setAppId(100); req.setFromId("userA"); req.setToId("userB");

            Result result = friendService.getRelation(req);

            assertFalse(result.isOk());
        }
    }

    @Nested
    @DisplayName("deleteAllFriend - 删除全部好友")
    class DeleteAllFriendTest {
        @Test
        @DisplayName("应标记删除并通知")
        void deletesAndNotifies() {
            var req = new com.vela.im.service.friendship.application.dto.req.DeleteFriendReq();
            req.setAppId(100); req.setFromId("user001");

            Result result = friendService.deleteAllFriend(req);

            assertTrue(result.isOk());
            verify(messageProducer).sendToUser(anyString(), anyInt(), anyString(), any(), any(), anyInt());
        }
    }

    @Nested
    @DisplayName("getAllFriendId - 获取好友ID列表")
    class GetAllFriendIdTest {
        @Test
        @DisplayName("应调用Mapper查询")
        void callsMapper() {
            friendService.getAllFriendId("user001", 100);
            verify(friendShipMapper).getAllFriendId("user001", 100);
        }
    }
}
