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
                                         @RequestParam String botName, @RequestParam String webhookUrl) {
        return botService.register(appId, botId, botName, webhookUrl);
    }

    @GetMapping("/list")
    public Result<List<ImBotEntity>> list(@RequestParam Integer appId) {
        return botService.list(appId);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long botId) {
        return botService.delete(botId);
    }
}
