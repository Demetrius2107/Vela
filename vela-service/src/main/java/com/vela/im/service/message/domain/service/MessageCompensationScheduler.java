package com.vela.im.service.message.domain.service;

import com.vela.im.service.message.domain.entity.ImMessageBodyEntity;
import com.vela.im.service.message.domain.entity.ImMessageHistoryEntity;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageBodyMapper;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageHistoryMapper;
import com.vela.im.service.group.domain.entity.ImGroupMessageHistoryEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupMessageHistoryMapper;
import com.vela.im.shared.types.message.GroupChatMessageContent;
import com.vela.im.shared.types.message.ImMessageBody;
import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Title: MessageCompensationScheduler</p>
 * <p>Description: 消息存储补偿调度器，定期扫描补偿队列中的失败消息并重试。
 * 对超过最大重试次数的消息放弃并打印告警日志。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Component
public class MessageCompensationScheduler {

    private static final Logger log = LoggerFactory.getLogger(MessageCompensationScheduler.class);

    private final MessageCompensationStore compensationStore;
    private final ImMessageBodyMapper imMessageBodyMapper;
    private final ImMessageHistoryMapper imMessageHistoryMapper;
    private final ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;
    private final MessageStoreService messageStoreService;

    public MessageCompensationScheduler(MessageCompensationStore compensationStore,
                                        ImMessageBodyMapper imMessageBodyMapper,
                                        ImMessageHistoryMapper imMessageHistoryMapper,
                                        ImGroupMessageHistoryMapper imGroupMessageHistoryMapper,
                                        MessageStoreService messageStoreService) {
        this.compensationStore = compensationStore;
        this.imMessageBodyMapper = imMessageBodyMapper;
        this.imMessageHistoryMapper = imMessageHistoryMapper;
        this.imGroupMessageHistoryMapper = imGroupMessageHistoryMapper;
        this.messageStoreService = messageStoreService;
    }

    /**
     * 每 15 秒扫描补偿队列，重试写入失败的 P2P/群聊消息。
     */
    @Scheduled(fixedDelay = 15000)
    public void retryCompensations() {
        List<MessageCompensationStore.CompensationEntry> entries = compensationStore.getAll();
        if (entries.isEmpty()) {
            return;
        }

        log.info("Compensation scheduler: retrying {} failed message(s)", entries.size());

        for (MessageCompensationStore.CompensationEntry entry : entries) {
            if (!entry.canRetry()) {
                log.error("Compensation exhausted for msgKey={}, type={}, retries={}, giving up",
                        entry.getMessageBody().getMessageKey(), entry.getType(), entry.getRetryCount());
                compensationStore.remove(entry);
                continue;
            }

            try {
                if (entry.getType() == 1) {
                    retryP2P(entry);
                } else {
                    retryGroup(entry);
                }
                // Success — remove from queue
                compensationStore.remove(entry);
                log.info("Compensation succeeded for msgKey={}, type={}, retry={}/{}",
                        entry.getMessageBody().getMessageKey(), entry.getType(),
                        entry.getRetryCount(), MessageCompensationStore.MAX_RETRIES);
            } catch (Exception e) {
                log.warn("Compensation retry failed for msgKey={}, type={}, retry={}/{}, error={}",
                        entry.getMessageBody().getMessageKey(), entry.getType(),
                        entry.getRetryCount(), MessageCompensationStore.MAX_RETRIES, e.getMessage());
            }
        }
    }

    private void retryP2P(MessageCompensationStore.CompensationEntry entry) {
        ImMessageBody messageBody = entry.getMessageBody();
        MessageContent content = entry.getP2pContent();
        if (content == null) return;

        // Check if already persisted (idempotent)
        ImMessageBodyEntity existing = imMessageBodyMapper.selectById(messageBody.getMessageKey());
        if (existing != null) {
            log.info("P2P message already exists in DB, skipping compensation, msgKey={}", messageBody.getMessageKey());
            return;
        }

        // Persist message body
        ImMessageBodyEntity bodyEntity = new ImMessageBodyEntity();
        BeanUtils.copyProperties(messageBody, bodyEntity);
        imMessageBodyMapper.insert(bodyEntity);

        // Persist message history
        List<ImMessageHistoryEntity> histories = messageStoreService.extractToP2PMessageHistory(content, bodyEntity);
        if (histories != null && !histories.isEmpty()) {
            imMessageHistoryMapper.insertBatchSomeColumn(histories);
        }
    }

    private void retryGroup(MessageCompensationStore.CompensationEntry entry) {
        ImMessageBody messageBody = entry.getMessageBody();
        GroupChatMessageContent content = entry.getGroupContent();
        if (content == null) return;

        // Check if already persisted
        ImMessageBodyEntity existing = imMessageBodyMapper.selectById(messageBody.getMessageKey());
        if (existing != null) {
            log.info("Group message already exists in DB, skipping compensation, msgKey={}", messageBody.getMessageKey());
            return;
        }

        // Persist message body
        ImMessageBodyEntity bodyEntity = new ImMessageBodyEntity();
        BeanUtils.copyProperties(messageBody, bodyEntity);
        imMessageBodyMapper.insert(bodyEntity);

        // Persist group message history
        ImGroupMessageHistoryEntity groupHistory = messageStoreService.extractToGroupMessageHistory(content, bodyEntity);
        if (groupHistory != null) {
            imGroupMessageHistoryMapper.insert(groupHistory);
        }
    }
}
