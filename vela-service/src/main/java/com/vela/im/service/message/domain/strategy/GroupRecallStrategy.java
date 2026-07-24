package com.vela.im.service.message.domain.strategy;

import com.alibaba.fastjson.JSONObject;
import com.vela.im.codec.pack.message.RecallMessageNotifyPack;
import com.vela.im.service.application.utils.ConversationIdGenerate;
import com.vela.im.service.application.utils.GroupMessageProducer;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.conversation.domain.service.ConversationService;
import com.vela.im.service.group.domain.service.ImGroupMemberService;
import com.vela.im.service.infrastructure.seq.RedisSeq;
import com.vela.im.shared.constants.ImConstants;
import com.vela.im.shared.types.enums.ConversationTypeEnum;
import com.vela.im.shared.types.enums.DelFlagEnum;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.OfflineMessageContent;
import com.vela.im.shared.types.message.RecallMessageContent;
import com.vela.im.service.message.domain.entity.ImMessageBodyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Title: GroupRecallStrategy</p>
 * <p>Description: 群聊消息撤回策略，将撤回通知写入每个群成员的离线队列并发送群通知。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@Component
public class GroupRecallStrategy implements RecallStrategy {

    private static final Logger logger = LoggerFactory.getLogger(GroupRecallStrategy.class);

    private final MessageProducer messageProducer;
    private final ConversationService conversationService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisSeq redisSeq;
    private final ImGroupMemberService imGroupMemberService;
    private final GroupMessageProducer groupMessageProducer;

    public GroupRecallStrategy(MessageProducer messageProducer,
                               ConversationService conversationService,
                               RedisTemplate<String, String> redisTemplate,
                               RedisSeq redisSeq,
                               ImGroupMemberService imGroupMemberService,
                               GroupMessageProducer groupMessageProducer) {
        this.messageProducer = messageProducer;
        this.conversationService = conversationService;
        this.redisTemplate = redisTemplate;
        this.redisSeq = redisSeq;
        this.imGroupMemberService = imGroupMemberService;
        this.groupMessageProducer = groupMessageProducer;
    }

    @Override
    public void recall(RecallMessageContent content, RecallMessageNotifyPack pack,
                       ImMessageBodyEntity body) {
        List<String> memberIds = imGroupMemberService.getGroupMemberId(
                content.getToId(), content.getAppId());

        long seq = redisSeq.doGetSeq(content.getAppId() + ":" + ImConstants.SeqImConstants.Message
                + ":" + ConversationIdGenerate.generateP2PId(content.getFromId(), content.getToId()));

        // ACK 给撤回发起方
        messageProducer.sendToUser(content.getFromId(), MessageCommand.MSG_RECALL_ACK, pack, content);
        // 同步给发送方的其他设备
        messageProducer.sendToUserExceptClient(content.getFromId(),
                MessageCommand.MSG_RECALL_NOTIFY, pack, content);

        // 遍历群成员，写入离线通知 + 推送
        for (String memberId : memberIds) {
            String toKey = content.getAppId() + ":" + ImConstants.RedisImConstants.OfflineMessage
                    + ":" + memberId;

            OfflineMessageContent offline = new OfflineMessageContent();
            offline.setDelFlag(DelFlagEnum.DELETE.getCode());
            BeanUtils.copyProperties(content, offline);
            offline.setConversationType(ConversationTypeEnum.GROUP.getCode());
            offline.setConversationId(conversationService.convertConversationId(
                    offline.getConversationType(), content.getFromId(), content.getToId()));
            offline.setMessageBody(body.getMessageBody());
            offline.setMessageSequence(seq);

            // 写入离线队列（带重试）
            for (int i = 0; i < 2; i++) {
                try {
                    redisTemplate.opsForZSet().add(toKey, JSONObject.toJSONString(offline), seq);
                    break;
                } catch (Exception e) {
                    logger.warn("Group recall ZSet add failed, memberId={}, attempt {}/2", memberId, i + 1);
                    if (i == 1) logger.error("Group recall ZSet exhausted, memberId={}", memberId);
                }
            }

            // 发送撤回通知（带重试）
            for (int i = 0; i < 2; i++) {
                try {
                    groupMessageProducer.producer(content.getFromId(),
                            MessageCommand.MSG_RECALL_NOTIFY, pack, content);
                    break;
                } catch (Exception e) {
                    logger.warn("Group recall notify failed, memberId={}, attempt {}/2", memberId, i + 1);
                    if (i == 1) logger.error("Group recall notify exhausted, memberId={}", memberId);
                }
            }
        }
    }
}
