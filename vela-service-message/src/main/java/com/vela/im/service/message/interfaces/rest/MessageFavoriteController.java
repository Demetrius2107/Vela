package com.vela.im.service.message.interfaces.rest;

import com.vela.im.service.message.domain.service.MessageFavoriteService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>Title: MessageFavoriteController</p>
 * <p>Description: 消息收藏 REST API——收藏/取消收藏/列表/检查。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@RestController
@RequestMapping("/v1/favorite")
public class MessageFavoriteController {

    private final MessageFavoriteService favoriteService;

    public MessageFavoriteController(MessageFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestParam Integer appId, @RequestParam String userId,
                            @RequestParam String conversationId, @RequestParam String fromId,
                            @RequestParam String content, @RequestParam Long messageTime) {
        return favoriteService.add(appId, userId, conversationId, fromId, content, messageTime);
    }

    @PostMapping("/remove")
    public Result<Void> remove(@RequestParam Integer appId, @RequestParam String userId,
                               @RequestParam String conversationId, @RequestParam Long messageTime) {
        return favoriteService.remove(appId, userId, conversationId, messageTime);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam Integer appId, @RequestParam String userId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return favoriteService.list(appId, userId, page, size);
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Integer appId, @RequestParam String userId,
                                  @RequestParam String conversationId, @RequestParam Long messageTime) {
        return favoriteService.isFavorited(appId, userId, conversationId, messageTime);
    }
}
