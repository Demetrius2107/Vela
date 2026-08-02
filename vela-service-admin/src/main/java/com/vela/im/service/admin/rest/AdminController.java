package com.vela.im.service.admin.rest;

import com.vela.im.service.admin.domain.service.AdminAuthService;
import com.vela.im.service.admin.domain.service.AdminService;
import com.vela.im.service.admin.domain.service.SystemConfigService;
import com.vela.im.service.admin.domain.AdminRoleConfig;
import com.vela.im.service.group.domain.entity.ImGroupEntity;
import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.bot.domain.service.BotService;
import com.vela.im.service.config.domain.service.FeatureFlagService;
import com.vela.im.service.config.domain.entity.FeatureFlagEntity;
import com.vela.im.service.user.domain.entity.ImUserDataEntity;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminAuthService adminAuthService;
    private final SystemConfigService systemConfigService;
    private final AdminRoleConfig adminRoleConfig;
    private final BotService botService;
    private final FeatureFlagService featureFlagService;

    public AdminController(AdminService adminService, AdminAuthService adminAuthService,
                           SystemConfigService systemConfigService, AdminRoleConfig adminRoleConfig,
                           BotService botService, FeatureFlagService featureFlagService) {
        this.adminService = adminService;
        this.adminAuthService = adminAuthService;
        this.systemConfigService = systemConfigService;
        this.adminRoleConfig = adminRoleConfig;
        this.botService = botService;
        this.featureFlagService = featureFlagService;
    }

    private boolean checkPerm(String role, String action) {
        return adminRoleConfig.hasPermission(role != null ? role : "auditor", action);
    }

    // ==================== 认证 ====================

    @PostMapping("/login")
    public Result<String> login(@RequestParam String userId, @RequestParam String password) {
        return adminAuthService.login(userId, password);
    }

    @PostMapping("/admins/create")
    public Result<Void> createAdmin(@RequestParam String userId, @RequestParam String password,
                                     @RequestParam(defaultValue = "operator") String role) {
        return adminAuthService.createAdmin(userId, password, role);
    }

    @GetMapping("/admins/list")
    public Result<java.util.List<com.vela.im.service.admin.domain.AdminUserEntity>> listAdmins(
            @RequestHeader(value = "X-Admin-Role", defaultValue = "auditor") String role) {
        if (!checkPerm(role, "admins")) return Result.fail(403, "无权限");
        return adminAuthService.listAdmins();
    }

    @PostMapping("/admins/toggle")
    public Result<Void> toggleAdmin(@RequestHeader(value = "X-Admin-Role", defaultValue = "auditor") String role,
                                     @RequestParam Long adminId) {
        if (!checkPerm(role, "admins")) return Result.fail(403, "无权限");
        return adminAuthService.toggleAdminStatus(adminId);
    }

    // ==================== 看板 ====================

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/message/trend")
    public Result<Map<String, Object>> messageTrend(@RequestParam(defaultValue = "7") int days) {
        return adminService.messageTrend(days);
    }

    @GetMapping("/groups/top")
    public Result<List<Map<String, Object>>> topGroups(@RequestParam(defaultValue = "10") int limit) {
        return adminService.topActiveGroups(limit);
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(@RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return adminService.listUsers(keyword, page, size);
    }

    @GetMapping("/users/detail")
    public Result<ImUserDataEntity> getUserDetail(@RequestParam String userId) {
        return adminService.getUserDetail(userId);
    }

    @PostMapping("/users/update")
    public Result<Void> updateUser(@RequestHeader("X-Admin-Role") String role,
                                    @RequestParam String userId,
                                    @RequestParam(required = false) String nickName,
                                    @RequestParam(required = false) Integer userSex,
                                    @RequestParam(required = false) String selfSignature,
                                    @RequestParam(required = false) String location) {
        if (!checkPerm(role, "users")) return Result.fail(403, "无权限");
        return adminService.updateUser(userId, nickName, userSex, selfSignature, location);
    }

    @PostMapping("/users/toggleForbidden")
    public Result<Void> toggleForbidden(@RequestHeader("X-Admin-Role") String role,
                                         @RequestParam String userId) {
        if (!checkPerm(role, "users")) return Result.fail(403, "无权限");
        return adminService.toggleForbidden(userId);
    }

    @PostMapping("/users/batchForbidden")
    public Result<Void> batchForbidden(@RequestHeader("X-Admin-Role") String role,
                                        @RequestBody List<String> userIds,
                                        @RequestParam boolean forbidden) {
        if (!checkPerm(role, "users")) return Result.fail(403, "无权限");
        return adminService.batchForbidden(userIds, forbidden);
    }

    // ==================== 登录日志 ====================

    @GetMapping("/users/loginLogs")
    public Result<Map<String, Object>> loginLogs(@RequestParam(required = false) String userId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return adminService.loginLogs(userId, page, size);
    }

    // ==================== 群组管理 ====================

    @GetMapping("/groups")
    public Result<Map<String, Object>> listGroups(@RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) Integer status) {
        return adminService.listGroups(keyword, page, size, status);
    }

    @GetMapping("/groups/detail")
    public Result<ImGroupEntity> getGroupDetail(@RequestParam String groupId) {
        return adminService.getGroupDetail(groupId);
    }

    @PostMapping("/groups/dissolve")
    public Result<Void> dissolveGroup(@RequestHeader("X-Admin-Role") String role,
                                       @RequestParam String groupId, @RequestParam Integer appId) {
        if (!checkPerm(role, "groups")) return Result.fail(403, "无权限");
        return adminService.dissolveGroup(groupId, appId);
    }

    // ==================== 消息审计 ====================

    @GetMapping("/messages/search")
    public Result<Map<String, Object>> searchMessages(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String userId,
                                                       @RequestParam(required = false) String groupId,
                                                       @RequestParam(required = false) Long startTime,
                                                       @RequestParam(required = false) Long endTime,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return adminService.searchMessages(keyword, userId, groupId, startTime, endTime, page, size);
    }

    // ==================== 操作日志 ====================

    @GetMapping("/operations")
    public Result<Map<String, Object>> operationLogs(@RequestParam(required = false) String operatorId,
                                                      @RequestParam(required = false) String action,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return adminService.operationLogs(operatorId, action, page, size);
    }

    // ==================== 系统配置 ====================

    @GetMapping("/configs")
    public Result<List<com.vela.im.service.admin.domain.SystemConfigEntity>> listConfigs() {
        return systemConfigService.listConfigs();
    }

    @PostMapping("/configs/update")
    public Result<Void> updateConfig(@RequestHeader("X-Admin-Role") String role,
                                      @RequestParam Long id, @RequestParam String value) {
        if (!checkPerm(role, "settings")) return Result.fail(403, "无权限");
        return systemConfigService.updateConfig(id, value);
    }

    // ==================== 趋势与导出 ====================

    @GetMapping("/users/trend")
    public Result<Map<String, Object>> userTrend(@RequestParam(defaultValue = "7") int days) {
        return adminService.userTrend(days);
    }

    @GetMapping("/groups/export")
    public Result<List<com.vela.im.service.group.domain.entity.ImGroupEntity>> exportGroups() {
        return adminService.exportGroups();
    }

    // ==================== Bot 管理 ====================

    @GetMapping("/bots")
    public Result<Map<String, Object>> listBots(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String keyword) {
        return adminService.listBots(page, size, keyword);
    }

    @PostMapping("/bots/create")
    public Result<ImBotEntity> createBot(@RequestHeader("X-Admin-Role") String role,
                                          @RequestParam Integer appId,
                                          @RequestParam String botId,
                                          @RequestParam String botName,
                                          @RequestParam(required = false) String webhookUrl,
                                          @RequestParam(required = false) String description,
                                          @RequestParam(required = false) String category) {
        if (!checkPerm(role, "bots")) return Result.fail(403, "无权限");
        return botService.register(appId, botId, botName, webhookUrl, description, category);
    }

    @PostMapping("/bots/toggle")
    public Result<Void> toggleBot(@RequestHeader("X-Admin-Role") String role,
                                   @RequestParam Long botId) {
        if (!checkPerm(role, "bots")) return Result.fail(403, "无权限");
        return botService.toggleStatus(botId);
    }

    @PostMapping("/bots/delete")
    public Result<Void> deleteBot(@RequestHeader("X-Admin-Role") String role,
                                   @RequestParam Long botId) {
        if (!checkPerm(role, "bots")) return Result.fail(403, "无权限");
        return botService.delete(botId);
    }

    // ==================== 功能开关管理 ====================

    @GetMapping("/feature-flags")
    public Result<java.util.List<FeatureFlagEntity>> listFeatureFlags() {
        return featureFlagService.listAll();
    }

    @PostMapping("/feature-flags/update")
    public Result<Void> updateFeatureFlag(@RequestHeader("X-Admin-Role") String role,
                                           @RequestParam Long id,
                                           @RequestParam(required = false) Integer enabled,
                                           @RequestParam(required = false) String userWhitelist) {
        if (!checkPerm(role, "settings")) return Result.fail(403, "无权限");
        return featureFlagService.update(id, enabled, userWhitelist);
    }
}
