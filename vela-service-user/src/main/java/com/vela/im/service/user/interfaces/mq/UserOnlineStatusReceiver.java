package com.vela.im.service.user.interfaces.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.vela.im.service.user.application.dto.UserStatusChangeNotifyContent;
import com.vela.im.service.user.domain.service.ImUserStatusService;
import com.vela.im.shared.constants.ImConstants;
import com.vela.im.shared.trace.TraceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * <p>Title: UserOnlineStatusReceiver</p>
 * <p>Description: 监听用户在线/离线状态变更消息，处理状态存储与通知。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Component
public class UserOnlineStatusReceiver {

    private static final Logger logger = LoggerFactory.getLogger(UserOnlineStatusReceiver.class);

    private final ImUserStatusService userStatusService;

    public UserOnlineStatusReceiver(ImUserStatusService userStatusService) {
        this.userStatusService = userStatusService;
    }

    /**
     * 监听用户在线状态变更队列
     * 由 TCP 网关推送用户上下线消息（UserEventCommand.USER_ONLINE_STATUS_CHANGE）
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = ImConstants.RabbitMQ.IM_TO_USER_SERVICE, durable = "true"),
                    exchange = @Exchange(value = ImConstants.RabbitMQ.IM_TO_USER_SERVICE, type = "topic"),
                    key = ImConstants.RabbitMQ.IM_TO_USER_SERVICE
            )
    )
    public void onMessage(@Payload Message message,
                          @Headers Map<String, Object> headers,
                          Channel channel) throws IOException {
        // 从 AMQP 消息头中解析 TraceId
        TraceIdContext.setFromAmqpHeaders(headers);

        try {
            String body = new String(message.getBody(), "utf-8");
            logger.info("Received user status change message: {}", body);

            // 解析消息体（格式：{"command":xxx, "data":{...}, "appId":xxx, ...}）
            JSONObject json = JSON.parseObject(body);
            String command = json.getString("command");

            // 只处理在线状态变更指令
            if (command != null && json.containsKey("data")) {
                JSONObject data = json.getJSONObject("data");
                UserStatusChangeNotifyContent content = new UserStatusChangeNotifyContent();
                content.setAppId(json.getInteger("appId"));
                content.setUserId(data.getString("userId"));
                content.setStatus(data.getInteger("status"));

                logger.info("Processing status change: userId={}, status={}", content.getUserId(), content.getStatus());
                userStatusService.processUserOnlineStatusNotify(content);
            }

            // 手动 ACK
            Long deliveryTag = (Long) headers.get("amqp_deliveryTag");
            if (deliveryTag != null) {
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception e) {
            logger.error("Failed to process user status change message", e);
            Long deliveryTag = (Long) headers.get("amqp_deliveryTag");
            if (deliveryTag != null) {
                channel.basicNack(deliveryTag, false, true);
            }
        } finally {
            TraceIdContext.clear();
        }
    }
}
