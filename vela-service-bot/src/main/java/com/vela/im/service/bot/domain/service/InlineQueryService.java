package com.vela.im.service.bot.domain.service;

import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bot 行内查询服务。用户输入 @bot query 时触发，
 * 转发到 Bot Webhook，返回实时建议列表。
 */
@Service
public class InlineQueryService {

    private static final Logger log = LoggerFactory.getLogger(InlineQueryService.class);

    private final BotService botService;
    private final HttpClient httpClient;

    public InlineQueryService(BotService botService) {
        this.botService = botService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @SuppressWarnings("unchecked")
    public Result<List<Map<String, String>>> query(Integer appId, String botId, String query, String userId) {
        ImBotEntity bot = botService.getByBotId(botId, appId);
        if (bot == null || bot.getStatus() != 1) {
            return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.BOT_UNAVAILABLE);
        }

        try {
            String body = "{\"appId\":" + appId
                    + ",\"query\":\"" + escapeJson(query)
                    + "\",\"userId\":\"" + escapeJson(userId)
                    + "\",\"type\":\"inline_query\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bot.getWebhookUrl()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {
                String respBody = response.body().trim();
                if (respBody.startsWith("[")) {
                    List<Map<String, String>> results = com.alibaba.fastjson.JSONObject.parseObject(
                            respBody, new com.alibaba.fastjson.TypeReference<List<Map<String, String>>>() {});
                    return Result.ok(results);
                }
            }
        } catch (Exception e) {
            log.warn("Inline query failed: botId={}, query={}", botId, query);
        }

        return Result.ok(new ArrayList<>());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
