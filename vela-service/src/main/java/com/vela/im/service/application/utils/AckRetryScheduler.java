package com.vela.im.service.application.utils;

import com.vela.im.shared.config.ImServerProperties;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Title: AckRetryScheduler</p>
 * <p>Description: ACK 超时重推定任务，定期扫描 PendingAckTracker 中超时未确认的消息，
 * 对未超重推上限的消息进行指数退避重推。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Component
public class AckRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(AckRetryScheduler.class);

    private final PendingAckTracker pendingAckTracker;
    private final MessageProducer messageProducer;
    private final ImServerProperties appConfig;

    public AckRetryScheduler(PendingAckTracker pendingAckTracker,
                             MessageProducer messageProducer,
                             ImServerProperties appConfig) {
        this.pendingAckTracker = pendingAckTracker;
        this.messageProducer = messageProducer;
        this.appConfig = appConfig;
    }

    /**
     * 每 5 秒扫描一次超时未 ACK 的推送，尝试重推。
     * 重推间隔 = min(5s × 2^retryCount, 30s) — 第 1 次 5s，第 2 次 10s，第 3 次 20s。
     */
    @Scheduled(fixedDelay = 5000)
    public void retryExpiredAcks() {
        long ackTimeoutMs = PendingAckTracker.DEFAULT_ACK_TIMEOUT_MS;
        List<PendingAckTracker.PendingEntry> expired = pendingAckTracker.getExpiredEntries(ackTimeoutMs);

        if (expired.isEmpty()) {
            return;
        }

        log.info("ACK retry scheduler: found {} expired pending ACKs", expired.size());

        for (PendingAckTracker.PendingEntry entry : expired) {
            if (entry.incrementRetry()) {
                // 重推给接收方
                MessageContent msg = entry.getMessage();
                log.warn("Retrying push for unacknowledged message: msgId={}, msgKey={}, toId={}, retry={}/{}",
                        msg.getMessageId(), msg.getMessageKey(), msg.getToId(),
                        entry.getRetryCount(), PendingAckTracker.MAX_RETRY_PUSH);
                try {
                    messageProducer.sendToUser(entry.getToId(),
                            MessageCommand.MSG_P2P, msg, entry.getAppId());
                    entry.setLastPushTime(System.currentTimeMillis());
                } catch (Exception e) {
                    log.error("ACK retry push failed, msgKey={}, toId={}, error={}",
                            entry.getMessageKey(), entry.getToId(), e.getMessage());
                }
            } else {
                // 重推耗尽，发服务端确认给发送方（最终一致性保障）
                log.warn("ACK retry exhausted for msgId={}, msgKey={}, toId={}, sending server ACK instead",
                        entry.getMessageId(), entry.getMessageKey(), entry.getToId());
                com.vela.im.codec.pack.message.MessageReciveServerAckPack serverAck =
                        new com.vela.im.codec.pack.message.MessageReciveServerAckPack();
                MessageContent msg = entry.getMessage();
                serverAck.setFromId(entry.getToId());
                serverAck.setToId(msg.getFromId());
                serverAck.setMessageKey(entry.getMessageKey());
                serverAck.setMessageSequence(msg.getMessageSequence());
                serverAck.setServerSend(true);
                messageProducer.sendToUser(msg.getFromId(), MessageCommand.MSG_RECIVE_ACK,
                        serverAck, new com.vela.im.shared.types.ClientInfo(
                                entry.getAppId(), msg.getClientType(), msg.getImei()));
                // 从跟踪器中移除
                pendingAckTracker.acknowledge(entry.getToId(), entry.getMessageKey());
            }
        }
    }
}
