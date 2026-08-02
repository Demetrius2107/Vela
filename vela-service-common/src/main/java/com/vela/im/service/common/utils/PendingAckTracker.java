package com.vela.im.service.common.utils;

import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Title: PendingAckTracker</p>
 * <p>Description: 消息接收 ACK 跟踪器，跟踪已推送给在线接收方但尚未收到 ACK 的消息。
 * 配合定时任务实现 ACK 丢失重推机制。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Component
public class PendingAckTracker {

    private static final Logger log = LoggerFactory.getLogger(PendingAckTracker.class);

    /** 默认 ACK 等待超时（毫秒） */
    public static final long DEFAULT_ACK_TIMEOUT_MS = 10000L;

    /** 最大重推次数 */
    public static final int MAX_RETRY_PUSH = 3;

    /**
     * 待确认的 ACK 条目。
     */
    public static class PendingEntry {
        private final String messageId;
        private final Long messageKey;
        private final String toId;
        private final Integer appId;
        private final MessageContent message;
        private final long firstPushTime;
        private volatile long lastPushTime;
        private final AtomicInteger retryCount = new AtomicInteger(0);

        public PendingEntry(String messageId, Long messageKey, String toId,
                            Integer appId, MessageContent message) {
            this.messageId = messageId;
            this.messageKey = messageKey;
            this.toId = toId;
            this.appId = appId;
            this.message = message;
            this.firstPushTime = System.currentTimeMillis();
            this.lastPushTime = this.firstPushTime;
        }

        public boolean incrementRetry() {
            return retryCount.incrementAndGet() <= MAX_RETRY_PUSH;
        }

        public String getMessageId() { return messageId; }
        public Long getMessageKey() { return messageKey; }
        public String getToId() { return toId; }
        public Integer getAppId() { return appId; }
        public MessageContent getMessage() { return message; }
        public long getFirstPushTime() { return firstPushTime; }
        public long getLastPushTime() { return lastPushTime; }
        public void setLastPushTime(long lastPushTime) { this.lastPushTime = lastPushTime; }
        public int getRetryCount() { return retryCount.get(); }
    }

    /** pendingMap: toId -> (messageKey -> PendingEntry) */
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, PendingEntry>> pendingMap = new ConcurrentHashMap<>();

    /**
     * 跟踪一条推送给在线接收方的消息。
     *
     * @param msg 已推送的消息
     */
    public void track(MessageContent msg) {
        if (msg == null || msg.getToId() == null || msg.getMessageKey() == null) {
            return;
        }
        ConcurrentHashMap<Long, PendingEntry> userPending = pendingMap
                .computeIfAbsent(msg.getToId(), k -> new ConcurrentHashMap<>());
        PendingEntry entry = new PendingEntry(
                msg.getMessageId(), msg.getMessageKey(), msg.getToId(),
                msg.getAppId(), msg);
        userPending.put(msg.getMessageKey(), entry);
        log.debug("ACK tracked: msgId={}, msgKey={}, toId={}", msg.getMessageId(), msg.getMessageKey(), msg.getToId());
    }

    /**
     * 收到接收方 ACK 后，清除跟踪记录。
     *
     * @param toId       接收方 ID
     * @param messageKey 消息 Key
     */
    public void acknowledge(String toId, Long messageKey) {
        if (toId == null || messageKey == null) {
            return;
        }
        ConcurrentHashMap<Long, PendingEntry> userPending = pendingMap.get(toId);
        if (userPending != null) {
            PendingEntry removed = userPending.remove(messageKey);
            if (removed != null) {
                log.debug("ACK cleared: msgKey={}, toId={}", messageKey, toId);
                // Clean up empty user maps
                if (userPending.isEmpty()) {
                    pendingMap.remove(toId, userPending);
                }
            }
        }
    }

    /**
     * 获取所有超时未 ACK 的待重推条目。
     *
     * @param timeoutMs 超时阈值（毫秒）
     * @return 超时待重推条目列表
     */
    public List<PendingEntry> getExpiredEntries(long timeoutMs) {
        List<PendingEntry> expired = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (ConcurrentHashMap<Long, PendingEntry> userPending : pendingMap.values()) {
            for (PendingEntry entry : userPending.values()) {
                if (now - entry.getLastPushTime() >= timeoutMs) {
                    expired.add(entry);
                }
            }
        }
        return expired;
    }

    /**
     * 获取所有待确认的条目数。
     */
    public int size() {
        int count = 0;
        for (ConcurrentHashMap<Long, PendingEntry> userPending : pendingMap.values()) {
            count += userPending.size();
        }
        return count;
    }
}
