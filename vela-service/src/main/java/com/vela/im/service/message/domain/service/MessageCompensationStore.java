package com.vela.im.service.message.domain.service;

import com.vela.im.shared.types.message.ImMessageBody;
import com.vela.im.shared.types.message.GroupChatMessageContent;
import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Title: MessageCompensationStore</p>
 * <p>Description: 消息存储补偿队列，当 MQ 降级写入 DB 也失败时，
 * 将失败消息暂存到内存队列，由定时任务异步重试。
 * 最多重试 5 次，超过则放弃（防止内存泄漏）。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Component
public class MessageCompensationStore {

    private static final Logger log = LoggerFactory.getLogger(MessageCompensationStore.class);

    /** 最大重试次数 */
    public static final int MAX_RETRIES = 5;

    /** 补偿条目 */
    public static class CompensationEntry {
        private final int type; // 1=P2P, 2=GROUP
        private final ImMessageBody messageBody;
        private final MessageContent p2pContent;
        private final GroupChatMessageContent groupContent;
        private final AtomicInteger retryCount = new AtomicInteger(0);
        private final long createdAt;

        public CompensationEntry(ImMessageBody messageBody, MessageContent content) {
            this.type = 1;
            this.messageBody = messageBody;
            this.p2pContent = content;
            this.groupContent = null;
            this.createdAt = System.currentTimeMillis();
        }

        public CompensationEntry(ImMessageBody messageBody, GroupChatMessageContent content) {
            this.type = 2;
            this.messageBody = messageBody;
            this.p2pContent = null;
            this.groupContent = content;
            this.createdAt = System.currentTimeMillis();
        }

        public boolean canRetry() {
            return retryCount.incrementAndGet() <= MAX_RETRIES;
        }

        public int getType() { return type; }
        public ImMessageBody getMessageBody() { return messageBody; }
        public MessageContent getP2pContent() { return p2pContent; }
        public GroupChatMessageContent getGroupContent() { return groupContent; }
        public int getRetryCount() { return retryCount.get(); }
        public long getCreatedAt() { return createdAt; }
    }

    private final ConcurrentLinkedQueue<CompensationEntry> queue = new ConcurrentLinkedQueue<>();

    /**
     * 将失败的 P2P 消息加入补偿队列。
     */
    public void compensate(ImMessageBody messageBody, MessageContent content) {
        queue.add(new CompensationEntry(messageBody, content));
        log.warn("P2P message added to compensation queue, msgKey={}, queueSize={}",
                messageBody.getMessageKey(), queue.size());
    }

    /**
     * 将失败的群聊消息加入补偿队列。
     */
    public void compensate(ImMessageBody messageBody, GroupChatMessageContent content) {
        queue.add(new CompensationEntry(messageBody, content));
        log.warn("Group message added to compensation queue, msgKey={}, queueSize={}",
                messageBody.getMessageKey(), queue.size());
    }

    /**
     * 获取所有待补偿的条目。
     */
    public List<CompensationEntry> getAll() {
        return new ArrayList<>(queue);
    }

    /**
     * 从补偿队列中移除已成功的条目。
     */
    public void remove(CompensationEntry entry) {
        queue.remove(entry);
    }

    /**
     * 当前补偿队列大小。
     */
    public int size() {
        return queue.size();
    }
}
