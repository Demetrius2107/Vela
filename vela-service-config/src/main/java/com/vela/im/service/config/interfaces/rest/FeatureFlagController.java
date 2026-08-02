package com.vela.im.service.config.interfaces.rest;

import com.vela.im.service.config.domain.entity.FeatureFlagEntity;
import com.vela.im.service.config.domain.service.FeatureFlagService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>Title: FeatureFlagController</p>
 * <p>Description: 功能开关 REST API——客户端拉取/管理后台CRUD。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@RestController
@RequestMapping("/v1/feature")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    /** 客户端拉取功能开关 */
    @GetMapping("/flags")
    public Result<Map<String, Boolean>> getFlags(@RequestParam Integer appId,
                                                  @RequestParam String userId) {
        return featureFlagService.getClientFlags(appId, userId);
    }

    /** 管理后台：列出所有开关 */
    @GetMapping("/admin/list")
    public Result<List<FeatureFlagEntity>> adminList() {
        return featureFlagService.listAll();
    }

    /** 管理后台：更新开关 */
    @PostMapping("/admin/update")
    public Result<Void> adminUpdate(@RequestParam Long id,
                                     @RequestParam(required = false) Integer enabled,
                                     @RequestParam(required = false) String userWhitelist) {
        return featureFlagService.update(id, enabled, userWhitelist);
    }
}
