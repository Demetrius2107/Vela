package com.vela.im.service.bot.domain.service;

import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.message.domain.service.MessageStoreService;
import com.vela.im.shared.types.enums.command.MessageCommand;
import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Bot 消息转发服务：检测消息目标是否为 Bot，将消息内容 JSON POST 到 Webhook，
 * 并将 Webhook 的响应文本作为 Bot 的回复发回给用户。
 */
@Service
public class BotMessageRelayService {

    private static final Logger log = LoggerFactory.getLogger(BotMessageRelayService.class);

    private final BotService botService;
    private final MessageProducer messageProducer;
    private final BotCommandRegistry commandRegistry;
    private final HttpClient httpClient;

    public BotMessageRelayService(BotService botService, MessageProducer messageProducer,
                                  BotCommandRegistry commandRegistry) {
        this.botService = botService;
        this.messageProducer = messageProducer;
        this.commandRegistry = commandRegistry;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 检查消息目标是否为 Bot，如果是则转发到 Webhook 并回发响应。
     *
     * @param message 用户发给 Bot 的消息
     * @return true=已处理（Bot 消息），false=不是 Bot 消息
     */
    public boolean relayToBotIfNeeded(MessageContent message) {
        ImBotEntity bot = botService.getByBotId(message.getToId(), message.getAppId());
        if (bot == null || bot.getStatus() != 1) return false;

        String body = message.getMessageBody();
        if (body != null && body.startsWith("/")) {
            handleCommand(message, bot, body);
            return true;
        }

        // 非指令消息，异步转发到 Webhook
        new Thread(() -> {
            try {
                String requestBody = buildRequestBody(message);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(bot.getWebhookUrl()))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Bot webhook response: botId={}, status={}, body={}",
                        bot.getBotId(), response.statusCode(), truncate(response.body(), 200));

                if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {
                    sendReply(bot, message, response.body());
                }
            } catch (Exception e) {
                log.error("Bot webhook relay failed: botId={}, error={}", bot.getBotId(), e.getMessage());
                sendReply(bot, message, "Bot 暂时无法回复，请稍后再试");
            }
        }).start();

        return true;
    }

    private String buildRequestBody(MessageContent msg) {
        return "{\"appId\":" + msg.getAppId()
                + ",\"fromId\":\"" + escapeJson(msg.getFromId())
                + "\",\"toId\":\"" + escapeJson(msg.getToId())
                + "\",\"messageId\":\"" + escapeJson(msg.getMessageId())
                + "\",\"messageBody\":\"" + escapeJson(msg.getMessageBody())
                + "\",\"messageTime\":" + msg.getMessageTime()
                + ",\"replyToMsgKey\":" + (msg.getReplyToMsgKey() != null ? msg.getReplyToMsgKey() : "null")
                + "}";
    }

    private void sendReply(ImBotEntity bot, MessageContent original, String replyText) {
        MessageContent reply = new MessageContent();
        reply.setAppId(original.getAppId());
        reply.setFromId(bot.getBotId());
        reply.setToId(original.getFromId());
        reply.setMessageBody(replyText);
        reply.setMessageTime(System.currentTimeMillis());
        reply.setReplyToMsgKey(original.getMessageKey());

        messageProducer.sendToUser(original.getFromId(), MessageCommand.MSG_P2P, reply, original.getAppId());
        log.info("Bot reply sent: botId={}, to={}, replyLen={}", bot.getBotId(), original.getFromId(), replyText.length());
    }

    private void handleCommand(MessageContent message, ImBotEntity bot, String body) {
        String[] parts = body.substring(1).split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

        BotCommandHandler handler = commandRegistry.get(cmd);
        String replyText;
        if (handler == null) {
            replyText = "未知指令: /" + cmd + "，发送 /help 查看可用指令";
        } else {
            replyText = handler.handle(message, args);
        }
        sendReply(bot, message, replyText);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
