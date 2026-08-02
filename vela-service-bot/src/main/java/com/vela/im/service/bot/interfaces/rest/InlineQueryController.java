package com.vela.im.service.bot.interfaces.rest;

import com.vela.im.service.bot.domain.service.InlineQueryService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/bot/inline")
public class InlineQueryController {

    private final InlineQueryService inlineQueryService;

    public InlineQueryController(InlineQueryService inlineQueryService) {
        this.inlineQueryService = inlineQueryService;
    }

    @GetMapping("/query")
    public Result<List<Map<String, String>>> query(@RequestParam Integer appId,
                                                    @RequestParam String botId,
                                                    @RequestParam String q,
                                                    @RequestParam String userId) {
        return inlineQueryService.query(appId, botId, q, userId);
    }
}
