package com.vela.im.service.admin.rest;

import com.vela.im.service.admin.domain.service.AdminService;
import com.vela.im.service.group.domain.entity.ImGroupEntity;
import com.vela.im.service.user.domain.entity.ImUserDataEntity;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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
    public Result<Void> updateUser(@RequestParam String userId,
                                    @RequestParam(required = false) String nickName,
                                    @RequestParam(required = false) Integer userSex,
                                    @RequestParam(required = false) String selfSignature,
                                    @RequestParam(required = false) String location) {
        return adminService.updateUser(userId, nickName, userSex, selfSignature, location);
    }

    @PostMapping("/users/toggleForbidden")
    public Result<Void> toggleForbidden(@RequestParam String userId) {
        return adminService.toggleForbidden(userId);
    }

    @PostMapping("/users/batchForbidden")
    public Result<Void> batchForbidden(@RequestBody List<String> userIds,
                                        @RequestParam boolean forbidden) {
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
    public Result<Void> dissolveGroup(@RequestParam String groupId, @RequestParam Integer appId) {
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
}
