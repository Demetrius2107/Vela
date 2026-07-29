package com.vela.im.service.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.vela.im.codec.pack.conversation.DeleteConversationPack;
import com.vela.im.codec.pack.conversation.UpdateConversationPack;
import com.vela.im.shared.constants.ImConstants;
import com.vela.im.shared.trace.TraceIdContext;
import com.vela.im.shared.types.ClientInfo;
import com.lld.im.common.ClientType;
import com.vela.im.shared.types.UserSession;
import com.vela.im.shared.types.enums.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendToUser(String toId, Command command, Object msg, Integer appId) {
        // 构建消息
        JSONObject o = new JSONObject();
        o.put("command", command.getCommand());
        o.put("data", msg);
        o.put("appId", appId);
        o.put("clientType", ClientType.WEB.getCode());
        o.put("toId", toId);
        o.put("extra", "");
        String body = o.toJSONString();

        // 发送到用户的队列
        rabbitTemplate.convertAndSend(
                ImConstants.RabbitMQ.IM_TO_MESSAGE_SERVICE + appId,
                "",
                body,
                buildTraceProcessor()
        );
    }

    public void sendToUserExceptClient(String toId, Command command, Object msg, ClientInfo clientInfo) {
        JSONObject o = new JSONObject();
        o.put("command", command.getCommand());
        o.put("data", msg);
        o.put("appId", clientInfo.getAppId());
        o.put("clientType", ClientType.WEB.getCode());
        o.put("toId", toId);
        o.put("imei", clientInfo.getImei());
        o.put("extra", "");
        String body = o.toJSONString();

        rabbitTemplate.convertAndSend(
                ImConstants.RabbitMQ.IM_TO_MESSAGE_SERVICE + clientInfo.getAppId(),
                "",
                body,
                buildTraceProcessor()
        );
    }

    private MessagePostProcessor buildTraceProcessor() {
        return message -> {
            String traceId = TraceIdContext.get();
            if (traceId != null && !traceId.isEmpty()) {
                message.getMessageProperties().setHeader(ImConstants.TraceId.MQ_HEADER_NAME, traceId);
            }
            return message;
        };
    }
}
