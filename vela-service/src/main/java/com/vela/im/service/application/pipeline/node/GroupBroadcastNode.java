package com.vela.im.service.application.pipeline.node;

import com.vela.im.codec.pack.message.ChatMessageAck;
import com.vela.im.service.application.pipeline.MessageContext;
import com.vela.im.service.application.pipeline.PipeChain;
import com.vela.im.service.application.pipeline.PipeNode;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.group.domain.service.ImGroupMemberService;
import com.vela.im.service.infrastructure.seq.RedisSeq;
import com.vela.im.service.message.domain.service.MessageStoreService;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.constants.ImConstants;
import com.vela.im.shared.types.enums.ConversationTypeEnum;
import com.vela.im.shared.types.enums.command.GroupEventCommand;
import com.vela.im.shared.types.message.GroupChatMessageContent;
import com.vela.im.shared.types.message.OfflineMessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: GroupBroadcastNode</p>
 * <p>Description: 管道节点 — 群聊消息广播，负责持久化+离线存储+ACK+同步+成员分发。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@Component
public class GroupBroadcastNode implements PipeNode<MessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(GroupBroadcastNode.class);

    private final MessageStoreService messageStoreService;
    private final MessageProducer messageProducer;
    private final RedisSeq redisSeq;
    private final ImGroupMemberService imGroupMemberService;

    public GroupBroadcastNode(MessageStoreService messageStoreService,
                              MessageProducer messageProducer,
                              RedisSeq redisSeq,
                              ImGroupMemberService imGroupMemberService) {
        this.messageStoreService = messageStoreService;
        this.messageProducer = messageProducer;
        this.redisSeq = redisSeq;
        this.imGroupMemberService = imGroupMemberService;
    }

    @Override
    public void process(MessageContext ctx, PipeChain<MessageContext> chain) {
        GroupChatMessageContent msg = (GroupChatMessageContent) ctx.getMessageContent();

        // 生成群聊序列号
        long seq = redisSeq.doGetSeq(msg.getAppId() + ":" + ImConstants.Sequence.GROUP_MESSAGE
                + msg.getGroupId());
        msg.setMessageSequence(seq);

        // 持久化群聊消息
        messageStoreService.storeGroupMessage(msg);

        // 获取群成员列表
        List<String> memberIds = imGroupMemberService.getGroupMemberId(msg.getGroupId(), msg.getAppId());
        msg.setMemberId(memberIds);

        // 离线消息存储
        OfflineMessageContent offlineMsg = new OfflineMessageContent();
        BeanUtils.copyProperties(msg, offlineMsg);
        offlineMsg.setToId(msg.getGroupId());
        messageStoreService.storeGroupOfflineMessage(offlineMsg, memberIds);

        // ACK 给发送方
        ChatMessageAck ack = new ChatMessageAck(msg.getMessageId());
        messageProducer.sendToUser(msg.getFromId(), GroupEventCommand.GROUP_MSG_ACK, Result.ok(ack), msg);

        // 同步给发送方的其他设备
        messageProducer.sendToUserExceptClient(msg.getFromId(), GroupEventCommand.MSG_GROUP, msg, msg);

        // 分发给群成员
        dispatchToMembers(msg);

        // 缓存用于幂等校验
        messageStoreService.setMessageFromMessageIdCache(msg.getAppId(), msg.getMessageId(), msg);

        logger.info("Group message broadcast: msgId={}, groupId={}, members={}",
                msg.getMessageId(), msg.getGroupId(), memberIds.size());
        chain.next(ctx);
    }

    private void dispatchToMembers(GroupChatMessageContent msg) {
        List<String> failedMembers = new ArrayList<>();
        for (String memberId : msg.getMemberId()) {
            if (memberId.equals(msg.getFromId())) {
                continue; // 跳过发送方自己（已在 syncToSender 中处理）
            }
            boolean success = false;
            for (int retry = 0; retry < 2; retry++) {
                try {
                    messageProducer.sendToUser(memberId, GroupEventCommand.MSG_GROUP, msg, msg.getAppId());
                    success = true;
                    break;
                } catch (Exception e) {
                    logger.warn("Group dispatch failed, memberId={}, msgId={}, retry={}",
                            memberId, msg.getMessageId(), retry + 1);
                    if (retry == 0) {
                        try { Thread.sleep(100L); } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt(); break;
                        }
                    }
                }
            }
            if (!success) {
                failedMembers.add(memberId);
                logger.error("Group dispatch exhausted, memberId={}, msgId={}", memberId, msg.getMessageId());
            }
        }
        if (!failedMembers.isEmpty()) {
            logger.warn("Group partial dispatch failure, msgId={}, groupId={}, failed={}/{}",
                    msg.getMessageId(), msg.getGroupId(), failedMembers.size(), msg.getMemberId().size() - 1);
        }
    }
}
