package com.vela.im.service.message.interfaces.rest;

import com.vela.im.service.message.domain.entity.ImStickerEntity;
import com.vela.im.service.message.domain.entity.ImStickerPackEntity;
import com.vela.im.service.message.domain.service.StickerService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/sticker")
public class StickerController {

    private final StickerService stickerService;

    public StickerController(StickerService stickerService) {
        this.stickerService = stickerService;
    }

    // ====== 贴纸包 ======

    @GetMapping("/pack/list")
    public Result<List<ImStickerPackEntity>> listPacks(@RequestParam Integer appId) {
        return stickerService.listPacks(appId);
    }

    @PostMapping("/pack/create")
    public Result<ImStickerPackEntity> createPack(@RequestParam Integer appId,
                                                  @RequestParam String name,
                                                  @RequestParam(required = false) String icon) {
        return stickerService.createPack(appId, name, icon);
    }

    @PostMapping("/pack/delete")
    public Result<Void> deletePack(@RequestParam Long packId) {
        return stickerService.deletePack(packId);
    }

    // ====== 贴纸 ======

    @GetMapping("/list")
    public Result<List<ImStickerEntity>> listByPack(@RequestParam Long packId) {
        return stickerService.listByPack(packId);
    }

    @PostMapping("/create")
    public Result<ImStickerEntity> create(@RequestParam Long packId,
                                          @RequestParam String name,
                                          @RequestParam String fileUrl,
                                          @RequestParam(required = false) Integer width,
                                          @RequestParam(required = false) Integer height) {
        return stickerService.create(packId, name, fileUrl, width, height);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long stickerId) {
        return stickerService.delete(stickerId);
    }
}
