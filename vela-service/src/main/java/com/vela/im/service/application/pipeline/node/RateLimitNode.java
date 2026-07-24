package com.vela.im.service.application.pipeline.node;

import com.vela.im.service.application.pipeline.MessageContext;
import com.vela.im.service.application.pipeline.PipeChain;
import com.vela.im.service.application.pipeline.PipeNode;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.config.AppConfig;
import com.vela.im.shared.types.enums.MessageErrorCode;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.MessageContent;
import com.vela.im.codec.pack.message.ChatMessageAck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: RateLimitNode</p>
 * <p>Description: 管道节点 — 用户消息频率限制，防止单个用户刷消息。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@Component
public class RateLimitNode implements PipeNode<MessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitNode.class);

    private final MessageProducer messageProducer;
    private final AppConfig appConfig;

    /** 用户消息频率缓存：userId → 上一次消息的纳秒时间戳 */
    private final ConcurrentHashMap<String, Long> rateLimiter = new ConcurrentHashMap<>();

    public RateLimitNode(MessageProducer messageProducer, AppConfig appConfig) {
        this.messageProducer = messageProducer;
        this.appConfig = appConfig;
    }

    @Override
    public void process(MessageContext ctx, PipeChain<MessageContext> chain) {
        MessageContent msg = ctx.getMessageContent();
        String fromId = msg.getFromId();

        int rateLimit = appConfig.getMessageRateLimit() != null ? appConfig.getMessageRateLimit() : 20;
        long now = System.nanoTime();
        long window = 1_000_000_000L; // 1s
        long minInterval = window / rateLimit;

        Long lastMsg = rateLimiter.get(fromId);
        if (lastMsg != null && (now - lastMsg) < minInterval) {
            logger.warn("Rate limit exceeded for user={}, interval={}ns", fromId, now - lastMsg);
            ChatMessageAck ack = new ChatMessageAck(msg.getMessageId(), msg.getMessageSequence());
            Result<ChatMessageAck> result = Result.fail(MessageErrorCode.MESSAGE_RATE_LIMITED);
            result.setData(ack);
            messageProducer.sendToUser(fromId, MessageCommand.MSG_ACK, result, msg);
            ctx.interrupt(MessageErrorCode.MESSAGE_RATE_LIMITED);
            return;
        }
        rateLimiter.put(fromId, now);

        chain.next(ctx);
    }
}
