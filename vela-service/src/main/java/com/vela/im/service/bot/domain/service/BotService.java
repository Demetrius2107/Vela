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
        bot.setStatus(com.vela.im.shared.types.enums.StatusConstants.BOT_ENABLED);
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
        if (bot == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.BOT_NOT_FOUND);
        bot.setStatus(bot.getStatus() == com.vela.im.shared.types.enums.StatusConstants.BOT_ENABLED ? com.vela.im.shared.types.enums.StatusConstants.BOT_DISABLED : com.vela.im.shared.types.enums.StatusConstants.BOT_ENABLED);
        botMapper.updateById(bot);
        return Result.ok();
    }

    public Result<String> regenerateApiKey(Long botId) {
        ImBotEntity bot = botMapper.selectById(botId);
        if (bot == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.BOT_NOT_FOUND);
        String newKey = UUID.randomUUID().toString().replace("-", "");
        bot.setApiKey(newKey);
        botMapper.updateById(bot);
        return Result.ok(newKey);
    }

    public Result<Void> updateWebhook(Long botId, String webhookUrl) {
        ImBotEntity bot = botMapper.selectById(botId);
        if (bot == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.BOT_NOT_FOUND);
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
        if (bot == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.BOT_NOT_FOUND);
        return Result.ok(bot);
    }
}
