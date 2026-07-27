package com.vela.im.service.bot.domain.service;

import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.bot.infrastructure.persistence.mapper.ImBotMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BotService {

    private final ImBotMapper botMapper;

    public BotService(ImBotMapper botMapper) {
        this.botMapper = botMapper;
    }

    public Result<ImBotEntity> register(Integer appId, String botId, String botName,
                                         String webhookUrl, String description) {
        ImBotEntity bot = new ImBotEntity();
        bot.setAppId(appId);
        bot.setBotId(botId);
        bot.setBotName(botName);
        bot.setWebhookUrl(webhookUrl);
        bot.setDescription(description);
        bot.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        bot.setStatus(1);
        bot.setCreateTime(System.currentTimeMillis());
        botMapper.insert(bot);
        return Result.ok(bot);
    }

    public Result<List<ImBotEntity>> list(Integer appId) {
        return Result.ok(botMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ImBotEntity>()
                        .eq("app_id", appId)));
    }

    public Result<Void> toggleStatus(Long botId) {
        ImBotEntity bot = botMapper.selectById(botId);
        if (bot == null) return Result.fail(500, "Bot不存在");
        bot.setStatus(bot.getStatus() == 1 ? 0 : 1);
        botMapper.updateById(bot);
        return Result.ok();
    }

    public Result<String> regenerateApiKey(Long botId) {
        ImBotEntity bot = botMapper.selectById(botId);
        if (bot == null) return Result.fail(500, "Bot不存在");
        String newKey = UUID.randomUUID().toString().replace("-", "");
        bot.setApiKey(newKey);
        botMapper.updateById(bot);
        return Result.ok(newKey);
    }

    public Result<Void> updateWebhook(Long botId, String webhookUrl) {
        ImBotEntity bot = botMapper.selectById(botId);
        if (bot == null) return Result.fail(500, "Bot不存在");
        bot.setWebhookUrl(webhookUrl);
        botMapper.updateById(bot);
        return Result.ok();
    }

    public Result<Void> delete(Long botId) {
        botMapper.deleteById(botId);
        return Result.ok();
    }

    public ImBotEntity getByBotId(String botId, Integer appId) {
        return botMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ImBotEntity>()
                .eq("bot_id", botId).eq("app_id", appId));
    }

    public Result<ImBotEntity> get(Long botId) {
        ImBotEntity bot = botMapper.selectById(botId);
        if (bot == null) return Result.fail(500, "Bot不存在");
        return Result.ok(bot);
    }
}
