package com.vela.im.service.application.pipeline.node;

import com.vela.im.service.application.pipeline.MessageContext;
import com.vela.im.service.application.pipeline.PipeChain;
import com.vela.im.service.application.pipeline.PipeNode;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.message.domain.service.MessageStoreService;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.MessageContent;
import com.vela.im.shared.types.message.OfflineMessageContent;
import com.vela.im.codec.pack.message.ChatMessageAck;
import com.vela.im.shared.types.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Title: DedupNode</p>
 * <p>Description: 管道节点 — 消息幂等校验，通过 messageId 缓存去重，避免重复处理。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@Component
public class DedupNode implements PipeNode<MessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(DedupNode.class);

    private final MessageStoreService messageStoreService;
    private final MessageProducer messageProducer;

    public DedupNode(MessageStoreService messageStoreService, MessageProducer messageProducer) {
        this.messageStoreService = messageStoreService;
        this.messageProducer = messageProducer;
    }

    @Override
    public void process(MessageContext ctx, PipeChain<MessageContext> chain) {
        MessageContent msg = ctx.getMessageContent();

        // 查询消息缓存
        MessageContent cached = messageStoreService.getMessageFromMessageIdCache(
                msg.getAppId(), msg.getMessageId(), MessageContent.class);

        if (cached != null) {
            // 幂等命中：直接 ACK + 同步 + 分发（不走完整管道）
            logger.info("Duplicate message detected, msgId={}, skip persistence", msg.getMessageId());
            ChatMessageAck ack = new ChatMessageAck(msg.getMessageId(), msg.getMessageSequence());
            messageProducer.sendToUser(msg.getFromId(), MessageCommand.MSG_ACK, Result.ok(ack), msg);
            messageProducer.sendToUserExceptClient(msg.getFromId(), MessageCommand.MSG_P2P, cached, msg);
            // 离线消息处理由缓存中的消息走推送
            ctx.interrupt("duplicate");
            return;
        }

        chain.next(ctx);
    }
}
