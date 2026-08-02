package com.vela.im.service.message.domain.pipeline.node;

import com.vela.im.service.common.pipeline.MessageContext;
import com.vela.im.service.common.pipeline.PipeChain;
import com.vela.im.service.common.pipeline.PipeNode;
import com.vela.im.service.common.utils.MessageProducer;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.config.ImServerProperties;
import com.vela.im.shared.types.enums.MessageErrorCode;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.MessageContent;
import com.vela.im.codec.pack.message.ChatMessageAck;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <p>Title: ValidateNode</p>
 * <p>Description: 管道节点 — 消息边界校验，检查字段合法性（空值、自发送、大小、时间）。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@Component
public class ValidateNode implements PipeNode<MessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(ValidateNode.class);

    private final MessageProducer messageProducer;
    private final ImServerProperties appConfig;

    public ValidateNode(MessageProducer messageProducer, ImServerProperties appConfig) {
        this.messageProducer = messageProducer;
        this.appConfig = appConfig;
    }

    @Override
    public void process(MessageContext ctx, PipeChain<MessageContext> chain) {
        MessageContent msg = ctx.getMessageContent();

        // fromId 不能为空
        if (StringUtils.isBlank(msg.getFromId())) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_FROMID_EMPTY);
            return;
        }
        // toId 不能为空
        if (StringUtils.isBlank(msg.getToId())) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_TOID_EMPTY);
            return;
        }
        // 消息体不能为空
        if (StringUtils.isBlank(msg.getMessageBody())) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_BODY_EMPTY);
            return;
        }
        // 不能给自己发
        if (msg.getFromId().equals(msg.getToId())) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_SELF_SEND);
            return;
        }
        // 消息体大小限制
        int maxSize = appConfig.getMessageMaxSize() != null ? appConfig.getMessageMaxSize() : 65536;
        if (msg.getMessageBody().getBytes().length > maxSize) {
            reject(ctx, msg, MessageErrorCode.MESSAGE_BODY_TOO_LARGE);
            return;
        }
        // 消息时间偏差不能过大
        if (msg.getMessageTime() != null) {
            long now = System.currentTimeMillis();
            long maxDeviation = appConfig.getMessageTimeMaxDeviation() != null
                    ? appConfig.getMessageTimeMaxDeviation() : 300000L;
            if (Math.abs(now - msg.getMessageTime()) > maxDeviation) {
                reject(ctx, msg, MessageErrorCode.MESSAGE_TIME_INVALID);
                return;
            }
        }

        // 校验通过，进入下一节点
        chain.next(ctx);
    }

    private void reject(MessageContext ctx, MessageContent msg, MessageErrorCode error) {
        logger.warn("Message rejected by boundary check, msgId={}, reason={}", msg.getMessageId(), error.getError());
        ChatMessageAck ack = new ChatMessageAck(msg.getMessageId(), msg.getMessageSequence());
        Result<ChatMessageAck> result = Result.fail(error);
        result.setData(ack);
        messageProducer.sendToUser(msg.getFromId(), MessageCommand.MSG_ACK, result, msg);
        ctx.interrupt(error);
    }
}
