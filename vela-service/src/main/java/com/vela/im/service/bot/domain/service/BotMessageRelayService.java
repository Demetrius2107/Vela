package com.vela.im.service.bot.domain.service;

import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.message.domain.service.MessageStoreService;
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
 * Bot 消息转发服务：当用户给 Bot 发消息时，将消息内容转发到 Bot 的 Webhook 地址，
 * 并将 Webhook 的响应作为 Bot 的回复发送回用户。
 */
@Service
public class BotMessageRelayService {

    private static final Logger log = LoggerFactory.getLogger(BotMessageRelayService.class);

    private final BotService botService;
    private final MessageStoreService messageStoreService;
    private final HttpClient httpClient;

    public BotMessageRelayService(BotService botService, MessageStoreService messageStoreService) {
        this.botService = botService;
        this.messageStoreService = messageStoreService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 检查消息目标是否为 Bot，如果是则转发到 Webhook。
     *
     * @param message 用户发送给 Bot 的消息
     * @return true=是 Bot 消息已处理, false=不是 Bot 消息
     */
    public boolean relayToBotIfNeeded(MessageContent message) {
        ImBotEntity bot = botService.getByBotId(message.getToId(), message.getAppId());
        if (bot == null) return false;

        // 异步转发到 Webhook
        new Thread(() -> {
            try {
                String body = "{\"appId\":" + message.getAppId()
                        + ",\"fromId\":\"" + message.getFromId()
                        + "\",\"toId\":\"" + message.getToId()
                        + "\",\"messageBody\":\"" + escapeJson(message.getMessageBody())
                        + "\",\"messageTime\":" + message.getMessageTime() + "}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(bot.getWebhookUrl()))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Bot webhook response: botId={}, status={}", bot.getBotId(), response.statusCode());
            } catch (Exception e) {
                log.error("Bot webhook relay failed: botId={}, error={}", bot.getBotId(), e.getMessage());
            }
        }).start();

        return true;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
