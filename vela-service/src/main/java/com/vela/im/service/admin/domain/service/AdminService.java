package com.vela.im.service.admin.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vela.im.service.admin.domain.AdminLoginLogEntity;
import com.vela.im.service.admin.domain.AdminOperationLogEntity;
import com.vela.im.service.admin.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.vela.im.service.admin.infrastructure.persistence.mapper.AdminOperationLogMapper;
import com.vela.im.service.message.infrastructure.elasticsearch.MessageSearchService;
import com.vela.im.service.group.domain.entity.ImGroupEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupMapper;
import com.vela.im.service.message.domain.entity.ImMessageBodyEntity;
import com.vela.im.service.message.domain.entity.ImMessageHistoryEntity;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageBodyMapper;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageHistoryMapper;
import com.vela.im.service.user.domain.entity.ImUserDataEntity;
import com.vela.im.service.user.infrastructure.persistence.mapper.ImUserDataMapper;
import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.bot.infrastructure.persistence.mapper.ImBotMapper;
import com.vela.im.shared.base.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final ImUserDataMapper userDataMapper;
    private final ImGroupMapper groupMapper;
    private final ImMessageBodyMapper messageBodyMapper;
    private final ImMessageHistoryMapper messageHistoryMapper;
    private final AdminLoginLogMapper loginLogMapper;
    private final AdminOperationLogMapper operationLogMapper;
    private final MessageSearchService messageSearchService;
    private final ImBotMapper botMapper;

    public AdminService(ImUserDataMapper userDataMapper, ImGroupMapper groupMapper,
                        ImMessageBodyMapper messageBodyMapper, ImMessageHistoryMapper messageHistoryMapper,
                        AdminLoginLogMapper loginLogMapper, AdminOperationLogMapper operationLogMapper,
                        MessageSearchService messageSearchService, ImBotMapper botMapper) {
        this.userDataMapper = userDataMapper;
        this.groupMapper = groupMapper;
        this.messageBodyMapper = messageBodyMapper;
        this.messageHistoryMapper = messageHistoryMapper;
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
        this.messageSearchService = messageSearchService;
        this.botMapper = botMapper;
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
        if (keyword != null && !keyword.isEmpty()) query.like("user_id", keyword).or().like("nick_name", keyword);
        IPage<ImUserDataEntity> p = userDataMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords()); result.put("total", p.getTotal()); result.put("pages", p.getPages());
        return Result.ok(result);
    }

    public Result<ImUserDataEntity> getUserDetail(String userId) {
        ImUserDataEntity user = userDataMapper.selectOne(new QueryWrapper<ImUserDataEntity>().eq("user_id", userId));
        if (user == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.USER_NOT_FOUND);
        return Result.ok(user);
    }

    @Transactional
    public Result<Void> updateUser(String userId, String nickName, Integer userSex, String selfSignature, String location) {
        ImUserDataEntity user = userDataMapper.selectOne(new QueryWrapper<ImUserDataEntity>().eq("user_id", userId));
        if (user == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.USER_NOT_FOUND);
        if (nickName != null) user.setNickName(nickName);
        if (userSex != null) user.setUserSex(userSex);
        if (selfSignature != null) user.setSelfSignature(selfSignature);
        if (location != null) user.setLocation(location);
        userDataMapper.update(user, new QueryWrapper<ImUserDataEntity>().eq("user_id", userId));
        logOp("system", "user_update", "user", userId, "更新用户资料");
        return Result.ok();
    }

    @Transactional
    public Result<Void> toggleForbidden(String userId) {
        ImUserDataEntity user = userDataMapper.selectOne(new QueryWrapper<ImUserDataEntity>().eq("user_id", userId));
        if (user == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.USER_NOT_FOUND);
        boolean wasForbidden = user.getForbiddenFlag() == 1;
        user.setForbiddenFlag(wasForbidden ? 0 : 1);
        userDataMapper.update(user, new QueryWrapper<ImUserDataEntity>().eq("user_id", userId));
        logOp("system", wasForbidden ? "user_unforbidden" : "user_forbidden", "user", userId, "切换禁用状态");
        return Result.ok();
    }

    @Transactional
    public Result<Void> batchForbidden(List<String> userIds, boolean forbidden) {
        for (String uid : userIds) {
            ImUserDataEntity user = userDataMapper.selectOne(new QueryWrapper<ImUserDataEntity>().eq("user_id", uid));
            if (user != null) {
                user.setForbiddenFlag(forbidden ? 1 : 0);
                userDataMapper.update(user, new QueryWrapper<ImUserDataEntity>().eq("user_id", uid));
            }
        }
        logOp("system", forbidden ? "batch_forbidden" : "batch_unforbidden", "user", String.join(",", userIds),
                "批量操作 " + userIds.size() + " 人");
        return Result.ok();
    }

    // ==================== 登录日志 ====================

    public Result<Map<String, Object>> loginLogs(String userId, int page, int size) {
        QueryWrapper<AdminLoginLogEntity> query = new QueryWrapper<>();
        if (userId != null && !userId.isEmpty()) query.eq("user_id", userId);
        query.orderByDesc("login_time");
        IPage<AdminLoginLogEntity> p = loginLogMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords()); result.put("total", p.getTotal());
        return Result.ok(result);
    }

    // ==================== 消息审计 ====================

    public Result<Map<String, Object>> searchMessages(String keyword, String userId, String groupId,
                                                       Long startTime, Long endTime, int page, int size) {
        if (keyword != null && !keyword.isEmpty()) {
            return messageSearchService.search(keyword, userId, page, size);
        }
        // 无关键词时降级为 SQL 查询
        QueryWrapper<ImMessageBodyEntity> query = new QueryWrapper<>();
        if (userId != null && !userId.isEmpty()) query.eq("from_id", userId);
        IPage<ImMessageBodyEntity> p = messageBodyMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords()); result.put("total", p.getTotal());
        return Result.ok(result);
    }

    // ==================== 群组管理 ====================

    public Result<Map<String, Object>> listGroups(String keyword, int page, int size, Integer status) {
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) query.like("group_id", keyword).or().like("group_name", keyword);
        if (status != null) query.eq("status", status);
        IPage<ImGroupEntity> p = groupMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords()); result.put("total", p.getTotal());
        return Result.ok(result);
    }

    public Result<ImGroupEntity> getGroupDetail(String groupId) {
        ImGroupEntity group = groupMapper.selectById(groupId);
        if (group == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.GROUP_NOT_FOUND);
        return Result.ok(group);
    }

    @Transactional
    public Result<Void> dissolveGroup(String groupId, Integer appId) {
        ImGroupEntity group = groupMapper.selectById(groupId);
        if (group == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.GROUP_NOT_FOUND);
        group.setStatus(com.vela.im.shared.types.enums.StatusConstants.GROUP_DISSOLVED);
        groupMapper.updateById(group);
        logOp("system", "group_dissolve", "group", groupId, "解散群组");
        return Result.ok();
    }

    // ==================== 趋势数据 ====================

    public Result<Map<String, Object>> messageTrend(int days) {
        long now = System.currentTimeMillis();
        long start = now - (long) days * 86400000L;
        long totalInPeriod = messageBodyMapper.selectCount(new QueryWrapper<ImMessageBodyEntity>().between("create_time", start, now));
        Map<String, Object> result = new HashMap<>();
        result.put("periodDays", days); result.put("totalMessages", totalInPeriod);
        result.put("avgPerDay", days > 0 ? totalInPeriod / days : totalInPeriod);
        return Result.ok(result);
    }

    public Result<List<Map<String, Object>>> topActiveGroups(int limit) {
        List<Map<String, Object>> top = messageHistoryMapper.selectMaps(
                new QueryWrapper<ImMessageHistoryEntity>()
                        .select("to_id as groupId, count(*) as msgCount")
                        .groupBy("to_id").orderByDesc("count(*)").last("LIMIT " + limit));
        return Result.ok(top);
    }

    // ==================== 操作日志 ====================

    public Result<Map<String, Object>> operationLogs(String operatorId, String action, int page, int size) {
        QueryWrapper<AdminOperationLogEntity> query = new QueryWrapper<>();
        if (operatorId != null && !operatorId.isEmpty()) query.eq("operator_id", operatorId);
        if (action != null && !action.isEmpty()) query.eq("action", action);
        query.orderByDesc("operate_time");
        IPage<AdminOperationLogEntity> p = operationLogMapper.selectPage(new Page<>(page + 1, size), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", p.getRecords()); result.put("total", p.getTotal());
        return Result.ok(result);
    }

    private void logOp(String operator, String action, String targetType, String targetId, String detail) {
        try {
            AdminOperationLogEntity log = new AdminOperationLogEntity();
            log.setOperatorId(operator != null ? operator : "system");
            log.setAction(action); log.setTargetType(targetType); log.setTargetId(targetId);
            log.setDetail(detail != null ? detail.substring(0, Math.min(detail.length(), 500)) : "");
            log.setOperateTime(System.currentTimeMillis());
            operationLogMapper.insert(log);
        } catch (Exception e) {
            logger.warn("Failed to log admin operation", e);
        }
    }

    // ==================== 趋势与导出 ====================

    public Result<Map<String, Object>> userTrend(int days) {
        long now = System.currentTimeMillis();
        long start = now - (long) days * 86400000L;
        List<Map<String, Object>> dailyData = userDataMapper.selectMaps(
                new QueryWrapper<ImUserDataEntity>()
                        .select("DATE_FORMAT(FROM_UNIXTIME(create_time/1000), '%Y-%m-%d') as date, count(*) as count")
                        .between("create_time", start, now)
                        .groupBy("date").orderByAsc("date"));
        Map<String, Object> result = new HashMap<>();
        result.put("days", days); result.put("data", dailyData);
        return Result.ok(result);
    }

    public Result<List<ImGroupEntity>> exportGroups() {
        return Result.ok(groupMapper.selectList(new QueryWrapper<ImGroupEntity>().orderByDesc("create_time")));
    }

    // ==================== Bot 管理 ====================

    public Result<Map<String, Object>> listBots(int page, int size, String keyword) {
        Page<ImBotEntity> p = new Page<>(page, size);
        QueryWrapper<ImBotEntity> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like("bot_name", keyword).or().like("bot_id", keyword);
        }
        qw.orderByDesc("id");
        IPage<ImBotEntity> result = botMapper.selectPage(p, qw);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return Result.ok(map);
    }
}
