package com.vela.im.service.bot.interfaces.rest;

import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.bot.domain.service.BotMarketService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>Title: BotMarketController</p>
 * <p>Description: Bot 市场 REST API——市场列表、分类、安装/卸载、我的Bot。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@RestController
@RequestMapping("/v1/bot/market")
public class BotMarketController {

    private final BotMarketService botMarketService;

    public BotMarketController(BotMarketService botMarketService) {
        this.botMarketService = botMarketService;
    }

    /** 市场列表 */
    @GetMapping("/list")
    public Result<List<ImBotEntity>> list(@RequestParam Integer appId,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) String keyword) {
        return botMarketService.marketList(appId, category, keyword);
    }

    /** 分类统计 */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories(@RequestParam Integer appId) {
        return botMarketService.categories(appId);
    }

    /** 安装 Bot */
    @PostMapping("/install")
    public Result<Void> install(@RequestParam Integer appId,
                                @RequestParam String userId,
                                @RequestParam String botId) {
        return botMarketService.install(appId, userId, botId);
    }

    /** 卸载 Bot */
    @PostMapping("/uninstall")
    public Result<Void> uninstall(@RequestParam Integer appId,
                                  @RequestParam String userId,
                                  @RequestParam String botId) {
        return botMarketService.uninstall(appId, userId, botId);
    }

    /** 我的 Bot 列表 */
    @GetMapping("/my")
    public Result<List<ImBotEntity>> myBots(@RequestParam Integer appId,
                                            @RequestParam String userId) {
        return botMarketService.myBots(appId, userId);
    }

    /** 检查是否已安装 */
    @GetMapping("/installed")
    public Result<Boolean> isInstalled(@RequestParam Integer appId,
                                       @RequestParam String userId,
                                       @RequestParam String botId) {
        return botMarketService.isInstalled(appId, userId, botId);
    }
}
