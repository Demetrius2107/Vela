package com.vela.im.service.config.interfaces.rest;

import com.vela.im.service.config.domain.service.UserConfigService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>Title: UserConfigController</p>
 * <p>Description: 用户个人配置 REST API——批量查询/保存。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@RestController
@RequestMapping("/v1/user/config")
public class UserConfigController {

    private final UserConfigService userConfigService;

    public UserConfigController(UserConfigService userConfigService) {
        this.userConfigService = userConfigService;
    }

    /** 批量获取用户配置 */
    @GetMapping("/get")
    public Result<Map<String, String>> getConfigs(@RequestParam Integer appId,
                                                   @RequestParam String userId,
                                                   @RequestParam(defaultValue = "all") String clientType) {
        return userConfigService.getConfigs(appId, userId, clientType);
    }

    /** 批量保存用户配置 */
    @PostMapping("/save")
    public Result<Void> saveConfigs(@RequestParam Integer appId,
                                    @RequestParam String userId,
                                    @RequestParam(defaultValue = "web") String clientType,
                                    @RequestBody List<Map<String, String>> configs) {
        return userConfigService.saveConfigs(appId, userId, clientType, configs);
    }
}
