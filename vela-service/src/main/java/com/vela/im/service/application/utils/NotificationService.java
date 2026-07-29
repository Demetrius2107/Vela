package com.vela.im.service.application.utils;

import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 桌面推送通知服务。当用户离线或消息需要提醒时，构造通知数据推送。
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final MessageProducer messageProducer;

    public NotificationService(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    /**
     * 推送新消息通知给接收方。
     * 通知数据由客户端 Web Notification API 解析展示。
     */
    public void notifyNewMessage(MessageContent msg) {
        try {
            com.vela.im.codec.pack.message.ChatMessageAck ack = new com.vela.im.codec.pack.message.ChatMessageAck(
                    msg.getMessageId(), msg.getMessageSequence());
            com.vela.im.shared.base.Result<com.vela.im.codec.pack.message.ChatMessageAck> result =
                    com.vela.im.shared.base.Result.ok(ack);
            result.setData(ack);
            messageProducer.sendToUser(msg.getToId(), com.vela.im.shared.types.enums.command.MessageCommand.MSG_P2P,
                    result, msg.getAppId());
        } catch (Exception e) {
            log.warn("Failed to send notification for msgId={}", msg.getMessageId());
        }
    }
}
