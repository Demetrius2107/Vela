package com.vela.im.service.bot.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.shared.types.enums.command.GroupEventCommand;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bot 消息转发服务。支持：
 * - 文本/富文本/图片回复
 * - 群聊 @bot 触发
 * - 速率限制
 * - 斜杠指令
 */
@Service
public class BotMessageRelayService {

    private static final Logger log = LoggerFactory.getLogger(BotMessageRelayService.class);

    private final BotService botService;
    private final MessageProducer messageProducer;
    private final BotCommandRegistry commandRegistry;
    private final HttpClient httpClient;

    /** 速率限制：botId → 上一次消息时间戳（纳秒） */
    private final ConcurrentHashMap<String, Long> rateLimiter = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_INTERVAL = 500_000_000L; // 0.5s between messages

    public BotMessageRelayService(BotService botService, MessageProducer messageProducer,
                                  BotCommandRegistry commandRegistry) {
        this.botService = botService;
        this.messageProducer = messageProducer;
        this.commandRegistry = commandRegistry;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /** 处理用户消息——检测是否为 Bot 消息 */
    public boolean relayToBotIfNeeded(MessageContent message) {
        ImBotEntity bot = botService.getByBotId(message.getToId(), message.getAppId());
        if (bot == null || bot.getStatus() != 1) return false;

        // 速率限制
        if (isRateLimited(bot.getBotId())) return true;

        String body = message.getMessageBody();
        if (body != null && body.startsWith("/")) {
            handleCommand(message, bot, body);
            return true;
        }

        // 异步转发到 Webhook
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
                log.info("Bot webhook response: botId={}, status={}", bot.getBotId(), response.statusCode());

                if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {
                    processWebhookResponse(bot, message, response.body());
                }
            } catch (Exception e) {
                log.error("Bot webhook relay failed: botId={}, error={}", bot.getBotId(), e.getMessage());
                sendReply(bot, message.getFromId(), message.getMessageKey(), "Bot 暂时无法回复，请稍后再试", null, null, null);
            }
        }).start();

        return true;
    }

    /** 处理群聊消息中的 Bot @提及 */
    public boolean handleGroupMention(MessageContent message, String groupId) {
        ImBotEntity bot = botService.getByBotId(message.getToId(), message.getAppId());
        if (bot == null || bot.getStatus() != 1) return false;
        if (isRateLimited(bot.getBotId())) return true;

        String body = message.getMessageBody();
        if (body != null && body.startsWith("/")) {
            handleCommand(message, bot, body);
            return true;
        }

        new Thread(() -> {
            try {
                String requestBody = "{\"appId\":" + message.getAppId()
                        + ",\"groupId\":\"" + escapeJson(groupId)
                        + "\",\"fromId\":\"" + escapeJson(message.getFromId())
                        + "\",\"messageBody\":\"" + escapeJson(message.getMessageBody())
                        + "\",\"messageTime\":" + message.getMessageTime()
                        + ",\"isGroup\":true}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(bot.getWebhookUrl()))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {
                    processWebhookResponse(bot, message, response.body());
                }
            } catch (Exception e) {
                log.error("Bot group mention failed: botId={}", bot.getBotId(), e);
            }
        }).start();
        return true;
    }

    /** 解析 Webhook 响应，支持纯文本、JSON 和按钮格式 */
    private void processWebhookResponse(ImBotEntity bot, MessageContent original, String responseBody) {
        String trimmed = responseBody.trim();
        if (trimmed.startsWith("{")) {
            try {
                JSONObject json = JSONObject.parseObject(trimmed);
                String type = json.getString("type");
                String content = json.getString("content");
                String fileUrl = json.getString("fileUrl");

                if ("image".equals(type) || "file".equals(type)) {
                    sendReply(bot, original.getFromId(), original.getMessageKey(),
                            content != null ? content : "[图片]", fileUrl, type, null);
                } else {
                    // 解析按钮
                    String buttons = null;
                    if (json.containsKey("buttons") && json.getJSONArray("buttons") != null) {
                        buttons = json.getJSONArray("buttons").toJSONString();
                    }
                    sendReply(bot, original.getFromId(), original.getMessageKey(),
                            content != null ? content : trimmed, null, null, buttons);
                }
                return;
            } catch (Exception ignored) {}
        }
        sendReply(bot, original.getFromId(), original.getMessageKey(), trimmed, null, null, null);
    }

    /** 发送回复消息（支持按钮） */
    private void sendReply(ImBotEntity bot, String toId, Long replyToMsgKey,
                           String text, String fileUrl, String fileType, String buttons) {
        MessageContent reply = new MessageContent();
        reply.setAppId(bot.getAppId());
        reply.setFromId(bot.getBotId());
        reply.setToId(toId);
        reply.setMessageBody(text);
        reply.setMessageTime(System.currentTimeMillis());
        reply.setReplyToMsgKey(replyToMsgKey);
        reply.setFileUrl(fileUrl);
        reply.setFileType(fileType);
        if (buttons != null) reply.setExtra(buttons);
        messageProducer.sendToUser(toId, MessageCommand.MSG_P2P, reply, bot.getAppId());
        log.info("Bot reply: botId={}, to={}, type={}, buttons={}",
                bot.getBotId(), toId, fileType != null ? fileType : "text", buttons != null ? "yes" : "no");
    }

    /** 处理按钮回调查询 */
    public void handleCallbackQuery(String callId, Integer appId, String userId, String botId, String callbackData) {
        ImBotEntity bot = botService.getByBotId(botId, appId);
        if (bot == null || bot.getStatus() != 1) return;

        new Thread(() -> {
            try {
                String requestBody = "{\"appId\":" + appId
                        + ",\"fromId\":\"" + escapeJson(userId)
                        + "\",\"botId\":\"" + escapeJson(botId)
                        + "\",\"callbackData\":\"" + escapeJson(callbackData)
                        + "\",\"type\":\"callback_query\"}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(bot.getWebhookUrl()))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {
                    // 用回调结果回复用户
                    MessageContent reply = new MessageContent();
                    reply.setAppId(appId);
                    reply.setFromId(botId);
                    reply.setToId(userId);
                    reply.setMessageBody(response.body());
                    reply.setMessageTime(System.currentTimeMillis());
                    messageProducer.sendToUser(userId, MessageCommand.MSG_P2P, reply, appId);
                }
            } catch (Exception e) {
                log.error("Bot callback query failed: botId={}, data={}", botId, callbackData, e);
            }
        }).start();
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
        sendReply(bot, message.getFromId(), message.getMessageKey(), replyText, null, null, null);
    }

    private boolean isRateLimited(String botId) {
        Long last = rateLimiter.get(botId);
        long now = System.nanoTime();
        if (last != null && (now - last) < RATE_LIMIT_INTERVAL) {
            log.warn("Bot rate limited: botId={}", botId);
            return true;
        }
        rateLimiter.put(botId, now);
        return false;
    }

    private String buildRequestBody(MessageContent msg) {
        return "{\"appId\":" + msg.getAppId()
                + ",\"fromId\":\"" + escapeJson(msg.getFromId())
                + "\",\"messageBody\":\"" + escapeJson(msg.getMessageBody())
                + "\",\"messageTime\":" + msg.getMessageTime()
                + ",\"replyToMsgKey\":" + (msg.getReplyToMsgKey() != null ? msg.getReplyToMsgKey() : "null")
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
