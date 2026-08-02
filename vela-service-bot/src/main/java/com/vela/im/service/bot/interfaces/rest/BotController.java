package com.vela.im.service.bot.interfaces.rest;

import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.bot.domain.service.BotService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/bot")
public class BotController {

    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping("/register")
    public Result<ImBotEntity> register(@RequestParam Integer appId, @RequestParam String botId,
                                         @RequestParam String botName, @RequestParam String webhookUrl,
                                         @RequestParam(required = false) String description) {
        return botService.register(appId, botId, botName, webhookUrl, description);
    }

    @GetMapping("/list")
    public Result<List<ImBotEntity>> list(@RequestParam Integer appId) {
        return botService.list(appId);
    }

    @GetMapping("/get")
    public Result<ImBotEntity> get(@RequestParam Long botId) {
        return botService.get(botId);
    }

    @PostMapping("/toggle")
    public Result<Void> toggle(@RequestParam Long botId) {
        return botService.toggleStatus(botId);
    }

    @PostMapping("/regen-key")
    public Result<String> regenKey(@RequestParam Long botId) {
        return botService.regenerateApiKey(botId);
    }

    @PostMapping("/update-webhook")
    public Result<Void> updateWebhook(@RequestParam Long botId, @RequestParam String webhookUrl) {
        return botService.updateWebhook(botId, webhookUrl);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long botId) {
        return botService.delete(botId);
    }
}
