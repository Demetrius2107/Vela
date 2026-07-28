package com.vela.im.service.admin.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.admin.domain.AdminRoleConfig;
import com.vela.im.service.admin.domain.AdminUserEntity;
import com.vela.im.service.admin.infrastructure.persistence.mapper.AdminUserMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleConfig roleConfig;

    public AdminAuthService(AdminUserMapper adminUserMapper, AdminRoleConfig roleConfig) {
        this.adminUserMapper = adminUserMapper;
        this.roleConfig = roleConfig;
    }

    /** 管理员登录 */
    public Result<String> login(String userId, String password) {
        AdminUserEntity admin = adminUserMapper.selectOne(
                new QueryWrapper<AdminUserEntity>().eq("user_id", userId));
        if (admin == null || !admin.getPassword().equals(password)) {
            return Result.fail(401, "管理员账号或密码错误");
        }
        if (admin.getStatus() != 1) {
            return Result.fail(403, "该管理员账号已被禁用");
        }
        // Generate simple token (in production, use JWT)
        String token = UUID.randomUUID().toString().replace("-", "");
        return Result.ok(token);
    }

    /** 创建管理员（仅 super_admin 可调用） */
    public Result<Void> createAdmin(String userId, String password, String role) {
        AdminUserEntity existing = adminUserMapper.selectOne(
                new QueryWrapper<AdminUserEntity>().eq("user_id", userId));
        if (existing != null) return Result.fail(500, "管理员已存在");

        AdminUserEntity admin = new AdminUserEntity();
        admin.setUserId(userId);
        admin.setPassword(password);
        admin.setRole(role != null ? role : roleConfig.getDefaultRole());
        admin.setStatus(1);
        admin.setCreateTime(System.currentTimeMillis());
        adminUserMapper.insert(admin);
        return Result.ok();
    }

    /** 获取管理员列表 */
    public Result<List<AdminUserEntity>> listAdmins() {
        return Result.ok(adminUserMapper.selectList(new QueryWrapper<>()));
    }

    /** 切换管理员状态 */
    public Result<Void> toggleAdminStatus(Long adminId) {
        AdminUserEntity admin = adminUserMapper.selectById(adminId);
        if (admin == null) return Result.fail(500, "管理员不存在");
        admin.setStatus(admin.getStatus() == 1 ? 0 : 1);
        adminUserMapper.updateById(admin);
        return Result.ok();
    }

    public boolean hasPermission(String role, String action) {
        return roleConfig.hasPermission(role, action);
    }
}
