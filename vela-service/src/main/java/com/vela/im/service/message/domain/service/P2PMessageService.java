package com.vela.im.service.message.domain.service;

import com.vela.im.service.application.pipeline.MessageContext;
import com.vela.im.service.application.pipeline.PipeChain;
import com.vela.im.service.application.pipeline.PipeNode;
import com.vela.im.service.application.pipeline.node.DedupNode;
import com.vela.im.service.application.pipeline.node.PersistAndPushNode;
import com.vela.im.service.application.pipeline.node.RateLimitNode;
import com.vela.im.service.application.pipeline.node.ValidateNode;
import com.vela.im.service.application.utils.CallbackService;
import com.vela.im.service.application.utils.ConversationIdGenerate;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.infrastructure.seq.RedisSeq;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.config.ImServerProperties;
import com.vela.im.shared.constants.ImConstants;
import com.vela.im.shared.types.ClientInfo;
import com.vela.im.shared.types.enums.ConversationTypeEnum;
import com.vela.im.shared.types.enums.MessageErrorCode;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.MessageContent;
import com.vela.im.shared.types.message.OfflineMessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Title: P2PMessageService</p>
 * <p>Description: 单聊消息处理服务，采用管道模式编排处理流程：
 * ValidateNode → RateLimitNode → DedupNode → PersistAndPushNode。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2025-03-06
 * @updateTime 2026-07-24
 *
 * Copyright © 2026 wanqiu All rights reserved
 */
@Service
public class P2PMessageService {

    private static final Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    /** 同步管道：校验+限流+去重 */
    private final PipeChain<MessageContext> syncPipeline;

    /** 异步管道：持久化+推送 */
    private final PersistAndPushNode persistAndPushNode;

    private final CheckSendMessageService checkSendMessageService;
    private final MessageProducer messageProducer;
    private final MessageStoreService messageStoreService;
    private final RedisSeq redisSeq;
    private final ImServerProperties appConfig;
    private final CallbackService callbackService;

    private final ThreadPoolExecutor threadPoolExecutor;

    public P2PMessageService(CheckSendMessageService checkSendMessageService,
                             MessageProducer messageProducer,
                             MessageStoreService messageStoreService,
                             RedisSeq redisSeq,
                             ImServerProperties appConfig,
                             CallbackService callbackService,
                             ValidateNode validateNode,
                             RateLimitNode rateLimitNode,
                             DedupNode dedupNode,
                             PersistAndPushNode persistAndPushNode) {
        this.checkSendMessageService = checkSendMessageService;
        this.messageProducer = messageProducer;
        this.messageStoreService = messageStoreService;
        this.redisSeq = redisSeq;
        this.appConfig = appConfig;
        this.callbackService = callbackService;
        this.persistAndPushNode = persistAndPushNode;

        // 装配同步管道：校验 → 限流 → 去重
        this.syncPipeline = new PipeChain<>(List.of(validateNode, rateLimitNode, dedupNode));

        // 异步线程池
        final AtomicInteger num = new AtomicInteger(0);
        this.threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("message-process-thread-" + num.getAndIncrement());
            return thread;
        });
    }

    /**
     * 处理单聊消息主流程。
     * <p>同步管道（校验+限流+去重）→ 通过后异步持久化+推送。</p>
     *
     * @param messageContent 消息内容
     */
    public void process(MessageContent messageContent) {
        logger.info("Processing message: msgId={}", messageContent.getMessageId());

        // 前置回调（判断在同步管道外，因为回调可能依赖完整上下文）
        if (appConfig.getCallback().isSendMessageAfterCallback()) {
            Result<?> callbackResult = callbackService.beforeCallback(
                    messageContent.getAppId(), ImConstants.CallbackCommand.SendMessageBefore,
                    com.alibaba.fastjson.JSONObject.toJSONString(messageContent));
            if (!callbackResult.isOk()) {
                com.vela.im.codec.pack.message.ChatMessageAck ack =
                        new com.vela.im.codec.pack.message.ChatMessageAck(
                                messageContent.getMessageId(), messageContent.getMessageSequence());
                messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,
                        Result.fail(callbackResult.getCode(), callbackResult.getMsg()), messageContent);
                return;
            }
        }

        // 创建上下文并启动同步管道
        MessageContext ctx = new MessageContext(messageContent);
        syncPipeline.process(ctx);

        // 管道被中断（校验失败/限流/重复消息），不继续
        if (ctx.isInterrupted()) {
            return;
        }

        // 通过同步管道，进入异步持久化+推送
        MessageContent msg = ctx.getMessageContent();
        threadPoolExecutor.execute(() -> {
            MessageContext asyncCtx = new MessageContext(msg);
            PipeChain<MessageContext> asyncPipe = new PipeChain<>(List.of(persistAndPushNode));
            asyncPipe.process(asyncCtx);
        });
    }

    /**
     * 检查发送方是否被禁言/禁用，以及好友关系。
     */
    public Result imServerPermissionCheck(String fromId, String toId, Integer appId) {
        Result<?> check = checkSendMessageService.checkSenderForvidAndMute(fromId, appId);
        if (!check.isOk()) {
            return check;
        }
        return checkSendMessageService.checkFriendShip(fromId, toId, appId);
    }

    /**
     * 同步发送消息（REST API 入口）。
     */
    public com.vela.im.service.message.application.dto.resp.SendMessageResp
    send(com.vela.im.service.message.application.dto.req.SendMessageReq req) {
        com.vela.im.service.message.application.dto.resp.SendMessageResp resp =
                new com.vela.im.service.message.application.dto.resp.SendMessageResp();
        MessageContent message = new MessageContent();
        org.springframework.beans.BeanUtils.copyProperties(req, message);
        messageStoreService.storeP2PMessage(message);
        resp.setMessageKey(message.getMessageKey());
        resp.setMessageTime(System.currentTimeMillis());
        // 同步给发送方其他设备并推送
        messageProducer.sendToUserExceptClient(message.getFromId(), MessageCommand.MSG_P2P, message, message);
        messageProducer.sendToUser(message.getToId(), MessageCommand.MSG_P2P, message, message.getAppId());
        return resp;
    }
}
