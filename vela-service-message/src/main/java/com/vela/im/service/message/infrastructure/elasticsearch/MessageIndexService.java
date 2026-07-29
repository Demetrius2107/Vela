package com.vela.im.service.message.infrastructure.elasticsearch;

import com.vela.im.shared.types.message.MessageContent;
import com.vela.im.shared.types.message.OfflineMessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 消息索引服务，将消息同步写入 Elasticsearch。
 * 索引失败不影响主流程（catch 后只打日志）。
 */
@Service
public class MessageIndexService {

    private static final Logger log = LoggerFactory.getLogger(MessageIndexService.class);

    private final MessageSearchRepository searchRepository;

    public MessageIndexService(MessageSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    /** 索引 P2P 消息 */
    public void indexMessage(MessageContent msg) {
        if (msg == null || msg.getMessageKey() == null) return;
        try {
            MessageDocument doc = new MessageDocument();
            doc.setMessageKey(msg.getMessageKey());
            doc.setMessageId(msg.getMessageId());
            doc.setFromId(msg.getFromId());
            doc.setToId(msg.getToId());
            doc.setMessageBody(msg.getMessageBody());
            doc.setMessageTime(msg.getMessageTime());
            doc.setAppId(msg.getAppId());
            doc.setConversationType(1); // P2P
            searchRepository.save(doc);
        } catch (Exception e) {
            log.warn("ES index failed for P2P message msgKey={}: {}", msg.getMessageKey(), e.getMessage());
        }
    }

    /** 索引离线消息 */
    public void indexOfflineMessage(OfflineMessageContent msg) {
        if (msg == null || msg.getMessageKey() == null) return;
        try {
            MessageDocument doc = new MessageDocument();
            doc.setMessageKey(msg.getMessageKey());
            doc.setFromId(msg.getFromId());
            doc.setToId(msg.getToId());
            doc.setMessageBody(msg.getMessageBody());
            doc.setMessageTime(msg.getMessageTime());
            doc.setAppId(msg.getAppId());
            doc.setConversationType(msg.getConversationType());
            searchRepository.save(doc);
        } catch (Exception e) {
            log.warn("ES index failed for offline msgKey={}: {}", msg.getMessageKey(), e.getMessage());
        }
    }
}
