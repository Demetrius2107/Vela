package com.vela.im.service.admin.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vela.im.service.admin.domain.AdminLoginLogEntity;
import com.vela.im.service.admin.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.vela.im.service.group.domain.entity.ImGroupEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupMapper;
import com.vela.im.service.message.domain.entity.ImMessageBodyEntity;
import com.vela.im.service.message.domain.entity.ImMessageHistoryEntity;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageBodyMapper;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageHistoryMapper;
import com.vela.im.service.user.domain.entity.ImUserDataEntity;
import com.vela.im.service.user.infrastructure.persistence.mapper.ImUserDataMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final ImUserDataMapper userDataMapper;
    private final ImGroupMapper groupMapper;
    private final ImMessageBodyMapper messageBodyMapper;
    private final ImMessageHistoryMapper messageHistoryMapper;
    private final AdminLoginLogMapper loginLogMapper;

    public AdminService(ImUserDataMapper userDataMapper, ImGroupMapper groupMapper,
                        ImMessageBodyMapper messageBodyMapper, ImMessageHistoryMapper messageHistoryMapper,
                        AdminLoginLogMapper loginLogMapper) {
        this.userDataMapper = userDataMapper;
        this.groupMapper = groupMapper;
        this.messageBodyMapper = messageBodyMapper;
        this.messageHistoryMapper = messageHistoryMapper;
        this.loginLogMapper = loginLogMapper;
    }

    // ==================== 看板 ====================

    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userDataMapper.selectCount(new QueryWrapper<>()));
        stats.put("totalGroups", groupMapper.selectCount(new QueryWrapper<>()));
        stats.put("totalMessages", messageBodyMapper.selectCount(new QueryWrapper<>()));
        stats.put("activeGroups", groupMapper.selectCount(new QueryWrapper<ImGroupEntity>().eq("status", 0)));
        stats.put("forbiddenUsers", userDataMapper.selectCount(new QueryWrapper<ImUserDataEntity>().eq("forbidden_flag", 1)));
        return Result.ok(stats);
    }

    // ==================== 用户管理 ====================

    public Result<Map<String, Object>> listUsers(String keyword, int page, int size) {
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("user_id", keyword).or().like("nick_name", keyword);
        }
        IPage<ImUserDataEntity> p = userDataMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        result.put("pages", p.getPages());
        return Result.ok(result);
    }

    public Result<ImUserDataEntity> getUserDetail(String userId) {
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        ImUserDataEntity user = userDataMapper.selectOne(query);
        if (user == null) return Result.fail(500, "用户不存在");
        return Result.ok(user);
    }

    public Result<Void> updateUser(String userId, String nickName, Integer userSex,
                                    String selfSignature, String location) {
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        ImUserDataEntity user = userDataMapper.selectOne(query);
        if (user == null) return Result.fail(500, "用户不存在");
        if (nickName != null) user.setNickName(nickName);
        if (userSex != null) user.setUserSex(userSex);
        if (selfSignature != null) user.setSelfSignature(selfSignature);
        if (location != null) user.setLocation(location);
        userDataMapper.update(user, query);
        return Result.ok();
    }

    @Transactional
    public Result<Void> toggleForbidden(String userId) {
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        ImUserDataEntity user = userDataMapper.selectOne(query);
        if (user == null) return Result.fail(500, "用户不存在");
        user.setForbiddenFlag(user.getForbiddenFlag() == 1 ? 0 : 1);
        userDataMapper.update(user, query);
        return Result.ok();
    }

    @Transactional
    public Result<Void> batchForbidden(List<String> userIds, boolean forbidden) {
        for (String uid : userIds) {
            QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
            query.eq("user_id", uid);
            ImUserDataEntity user = userDataMapper.selectOne(query);
            if (user != null) {
                user.setForbiddenFlag(forbidden ? 1 : 0);
                userDataMapper.update(user, query);
            }
        }
        return Result.ok();
    }

    // ==================== 登录日志 ====================

    public Result<Map<String, Object>> loginLogs(String userId, int page, int size) {
        QueryWrapper<AdminLoginLogEntity> query = new QueryWrapper<>();
        if (userId != null && !userId.isEmpty()) {
            query.eq("user_id", userId);
        }
        query.orderByDesc("login_time");
        IPage<AdminLoginLogEntity> p = loginLogMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        return Result.ok(result);
    }

    // ==================== 消息审计 ====================

    public Result<Map<String, Object>> searchMessages(String keyword, String userId,
                                                       String groupId, Long startTime, Long endTime,
                                                       int page, int size) {
        QueryWrapper<ImMessageBodyEntity> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("message_body", keyword);
        }
        if (userId != null && !userId.isEmpty()) {
            query.eq("from_id", userId);
        }
        IPage<ImMessageBodyEntity> p = messageBodyMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        return Result.ok(result);
    }

    // ==================== 群组管理 ====================

    public Result<Map<String, Object>> listGroups(String keyword, int page, int size, Integer status) {
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("group_id", keyword).or().like("group_name", keyword);
        }
        if (status != null) query.eq("status", status);
        IPage<ImGroupEntity> p = groupMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords());
        result.put("total", p.getTotal());
        return Result.ok(result);
    }

    public Result<ImGroupEntity> getGroupDetail(String groupId) {
        ImGroupEntity group = groupMapper.selectById(groupId);
        if (group == null) return Result.fail(500, "群组不存在");
        return Result.ok(group);
    }

    @Transactional
    public Result<Void> dissolveGroup(String groupId, Integer appId) {
        ImGroupEntity group = groupMapper.selectById(groupId);
        if (group == null) return Result.fail(500, "群组不存在");
        group.setStatus(1);
        groupMapper.updateById(group);
        return Result.ok();
    }

    // ==================== 趋势数据 ====================

    public Result<Map<String, Object>> messageTrend(int days) {
        long now = System.currentTimeMillis();
        long start = now - (long) days * 86400000L;
        QueryWrapper<ImMessageBodyEntity> query = new QueryWrapper<>();
        query.between("create_time", start, now);
        long totalInPeriod = messageBodyMapper.selectCount(query);
        Map<String, Object> result = new HashMap<>();
        result.put("periodDays", days);
        result.put("totalMessages", totalInPeriod);
        result.put("avgPerDay", days > 0 ? totalInPeriod / days : totalInPeriod);
        return Result.ok(result);
    }

    public Result<List<Map<String, Object>>> topActiveGroups(int limit) {
        // Get top groups by message count
        List<Map<String, Object>> top = messageHistoryMapper.selectMaps(
                new QueryWrapper<ImMessageHistoryEntity>()
                        .select("to_id as groupId, count(*) as msgCount")
                        .groupBy("to_id")
                        .orderByDesc("count(*)")
                        .last("LIMIT " + limit));
        return Result.ok(top);
    }
}
