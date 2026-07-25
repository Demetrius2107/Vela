package com.vela.im.service.application.pipeline.node;

import com.vela.im.codec.pack.message.ChatMessageAck;
import com.vela.im.codec.pack.message.MessageReciveServerAckPack;
import com.vela.im.service.application.pipeline.MessageContext;
import com.vela.im.service.application.pipeline.PipeChain;
import com.vela.im.service.application.pipeline.PipeNode;
import com.vela.im.service.application.utils.CallbackService;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.infrastructure.seq.RedisSeq;
import com.vela.im.service.message.domain.service.MessageStoreService;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.config.ImServerProperties;
import com.vela.im.shared.constants.ImConstants;
import com.vela.im.shared.types.ClientInfo;
import com.vela.im.shared.types.enums.ConversationTypeEnum;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.MessageContent;
import com.vela.im.shared.types.message.OfflineMessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Title: PersistAndPushNode</p>
 * <p>Description: 管道节点 — 消息持久化 + 离线存储 + ACK + 多端同步 + 推送 + 后置回调。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
@Component
public class PersistAndPushNode implements PipeNode<MessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(PersistAndPushNode.class);

    private final MessageStoreService messageStoreService;
    private final MessageProducer messageProducer;
    private final RedisSeq redisSeq;
    private final CallbackService callbackService;
    private final ImServerProperties appConfig;

    public PersistAndPushNode(MessageStoreService messageStoreService,
                              MessageProducer messageProducer,
                              RedisSeq redisSeq,
                              CallbackService callbackService,
                              ImServerProperties appConfig) {
        this.messageStoreService = messageStoreService;
        this.messageProducer = messageProducer;
        this.redisSeq = redisSeq;
        this.callbackService = callbackService;
        this.appConfig = appConfig;
    }

    @Override
    public void process(MessageContext ctx, PipeChain<MessageContext> chain) {
        MessageContent msg = ctx.getMessageContent();

        // 生成消息序列号
        long seq = redisSeq.doGetSeq(msg.getAppId() + ":"
                + ImConstants.Sequence.MESSAGE + ":"
                + msg.getFromId() + ":" + msg.getToId());
        msg.setMessageSequence(seq);

        // 持久化消息体 & 历史
        messageStoreService.storeP2PMessage(msg);

        // 离线消息存储
        OfflineMessageContent offlineMsg = new OfflineMessageContent();
        BeanUtils.copyProperties(msg, offlineMsg);
        offlineMsg.setConversationType(ConversationTypeEnum.P2P.getCode());
        messageStoreService.storeOfflineMessage(offlineMsg);

        // ACK 给发送方
        ChatMessageAck ack = new ChatMessageAck(msg.getMessageId(), msg.getMessageSequence());
        messageProducer.sendToUser(msg.getFromId(), MessageCommand.MSG_ACK, Result.ok(ack), msg);

        // 同步给发送方的其他设备
        messageProducer.sendToUserExceptClient(msg.getFromId(), MessageCommand.MSG_P2P, msg, msg);

        // 推送给接收方
        List<ClientInfo> onlineClients = dispatchToReceiver(msg);

        // 缓存用于幂等校验
        messageStoreService.setMessageFromMessageIdCache(msg.getAppId(), msg.getMessageId(), msg);

        // 接收方不在线，发服务端确认
        if (onlineClients.isEmpty()) {
            MessageReciveServerAckPack serverAck = new MessageReciveServerAckPack();
            serverAck.setFromId(msg.getToId());
            serverAck.setToId(msg.getFromId());
            serverAck.setMessageKey(msg.getMessageKey());
            serverAck.setMessageSequence(msg.getMessageSequence());
            serverAck.setServerSend(true);
            messageProducer.sendToUser(msg.getFromId(), MessageCommand.MSG_RECIVE_ACK, serverAck,
                    new ClientInfo(msg.getAppId(), msg.getClientType(), msg.getImei()));
        }

        // 后置回调
        if (appConfig.getCallback().isSendMessageAfterCallback()) {
            callbackService.callback(msg.getAppId(), ImConstants.CallbackCommand.SEND_MESSAGE_AFTER,
                    com.alibaba.fastjson.JSONObject.toJSONString(msg));
        }

        logger.info("Message processed: msgId={}, seq={}", msg.getMessageId(), seq);
        chain.next(ctx);
    }

    private List<ClientInfo> dispatchToReceiver(MessageContent msg) {
        try {
            return retryDispatch(msg);
        } catch (Exception e) {
            logger.error("Failed to dispatch message, msgId={}, toId={}", msg.getMessageId(), msg.getToId(), e);
            return List.of();
        }
    }

    private List<ClientInfo> retryDispatch(MessageContent msg) {
        List<ClientInfo> clients = messageProducer.sendToUser(
                msg.getToId(), MessageCommand.MSG_P2P, msg, msg.getAppId());
        if (clients == null) {
            throw new RuntimeException("dispatch returned null");
        }
        return clients;
    }
}
