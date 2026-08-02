package com.vela.im.service.group.domain.pipeline.node;

import com.vela.im.codec.pack.message.ChatMessageAck;
import com.vela.im.service.common.pipeline.MessageContext;
import com.vela.im.service.common.pipeline.PipeChain;
import com.vela.im.service.common.pipeline.PipeNode;
import com.vela.im.service.common.utils.MessageProducer;
import com.vela.im.service.group.domain.service.ImGroupMemberService;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.config.ImServerProperties;
import com.vela.im.shared.types.enums.MessageErrorCode;
import com.vela.im.shared.types.enums.command.GroupEventCommand;
import com.vela.im.shared.types.message.GroupChatMessageContent;
import com.vela.im.shared.types.message.MessageContent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <p>Title: GroupValidateNode</p>
 * <p>Description: 管道节点 — 群聊消息边界校验，检查字段合法性（空值、大小、时间）。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@Component
public class GroupValidateNode implements PipeNode<MessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(GroupValidateNode.class);

    private final MessageProducer messageProducer;
    private final ImServerProperties appConfig;
    private final ImGroupMemberService imGroupMemberService;

    public GroupValidateNode(MessageProducer messageProducer, ImServerProperties appConfig,
                             ImGroupMemberService imGroupMemberService) {
        this.messageProducer = messageProducer;
        this.appConfig = appConfig;
        this.imGroupMemberService = imGroupMemberService;
    }

    @Override
    public void process(MessageContext ctx, PipeChain<MessageContext> chain) {
        MessageContent mc = ctx.getMessageContent();
        if (!(mc instanceof GroupChatMessageContent)) {
            ctx.interrupt("not a group message");
            return;
        }
        GroupChatMessageContent msg = (GroupChatMessageContent) mc;

        if (StringUtils.isBlank(msg.getFromId())) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_FROMID_EMPTY);
            return;
        }
        if (StringUtils.isBlank(msg.getGroupId())) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_TOID_EMPTY);
            return;
        }
        if (StringUtils.isBlank(msg.getMessageBody())) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_BODY_EMPTY);
            return;
        }

        int maxSize = appConfig.getMessageMaxSize() != null ? appConfig.getMessageMaxSize() : 65536;
        if (msg.getMessageBody().getBytes().length > maxSize) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_BODY_TOO_LARGE);
            return;
        }

        if (msg.getMessageTime() != null) {
            long now = System.currentTimeMillis();
            long maxDeviation = appConfig.getMessageTimeMaxDeviation() != null
                    ? appConfig.getMessageTimeMaxDeviation() : 300000L;
            if (Math.abs(now - msg.getMessageTime()) > maxDeviation) {
                reject(ctx, msg, MessageErrorCode.MESSAGE_TIME_INVALID);
                return;
            }
        }

        // @所有人权限校验：仅管理员或群主可触发
        if (msg.isMentionAll()) {
            com.vela.im.shared.base.Result<com.vela.im.service.group.application.dto.resp.GetRoleInGroupResp> roleResult =
                    imGroupMemberService.getRoleInGroupOne(msg.getGroupId(), msg.getFromId(), msg.getAppId());
            if (!roleResult.isOk() || roleResult.getData() == null) {
                reject(ctx, msg, MessageErrorCode.MESSAGE_SEND_FAILED);
                return;
            }
            Integer role = roleResult.getData().getRole();
            // role: 0=普通成员, 1=管理员, 2=群主
            if (role < 1) {
                logger.warn("@all rejected: user={} is not admin/owner of group={}", msg.getFromId(), msg.getGroupId());
                reject(ctx, msg, MessageErrorCode.MESSAGE_SEND_FAILED);
                return;
            }
        }

        chain.next(ctx);
    }

    private void reject(MessageContext ctx, GroupChatMessageContent msg, MessageErrorCode error) {
        logger.warn("Group message rejected by boundary check, msgId={}, reason={}",
                msg.getMessageId(), error.getError());
        ChatMessageAck ack = new ChatMessageAck(msg.getMessageId());
        Result<ChatMessageAck> result = Result.fail(error);
        result.setData(ack);
        messageProducer.sendToUser(msg.getFromId(), GroupEventCommand.GROUP_MSG_ACK, result, msg);
        ctx.interrupt(error);
    }
}
