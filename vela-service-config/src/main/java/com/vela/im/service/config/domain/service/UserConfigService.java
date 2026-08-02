package com.vela.im.service.config.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.config.domain.entity.UserConfigEntity;
import com.vela.im.service.config.infrastructure.persistence.mapper.UserConfigMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Title: UserConfigService</p>
 * <p>Description: 用户个人配置服务，支持按用户批量查询/保存，跨端同步。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Service
public class UserConfigService {

    private final UserConfigMapper configMapper;

    public UserConfigService(UserConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    /**
     * 批量查询用户配置，支持前缀匹配（如 notify.*）
     */
    public Result<Map<String, String>> getConfigs(Integer appId, String userId, String clientType) {
        QueryWrapper<UserConfigEntity> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("app_id", appId);
        List<UserConfigEntity> list = configMapper.selectList(qw);

        Map<String, String> result = new HashMap<>();
        for (UserConfigEntity e : list) {
            // clientType=all 或匹配当前端 则返回
            if ("all".equals(e.getClientType()) || e.getClientType() == null || e.getClientType().equals(clientType)) {
                result.put(e.getConfigKey(), e.getConfigValue());
            }
        }
        return Result.ok(result);
    }

    /**
     * 批量保存用户配置（全量覆盖）
     */
    @Transactional
    public Result<Void> saveConfigs(Integer appId, String userId, String clientType,
                                     List<Map<String, String>> configs) {
        // 删除该用户+端的旧配置（避免残留）
        configMapper.delete(new QueryWrapper<UserConfigEntity>()
                .eq("user_id", userId)
                .eq("app_id", appId)
                .eq("client_type", clientType));

        // 批量插入新配置
        long now = System.currentTimeMillis();
        for (Map<String, String> entry : configs) {
            UserConfigEntity entity = new UserConfigEntity();
            entity.setAppId(appId);
            entity.setUserId(userId);
            entity.setConfigKey(entry.get("key"));
            entity.setConfigValue(entry.get("value"));
            entity.setClientType(clientType);
            entity.setUpdateTime(now);
            configMapper.insert(entity);
        }
        return Result.ok();
    }

    /**
     * 获取单个配置值
     */
    public String getConfig(Integer appId, String userId, String key, String defaultValue) {
        UserConfigEntity entity = configMapper.selectOne(
                new QueryWrapper<UserConfigEntity>()
                        .eq("user_id", userId)
                        .eq("app_id", appId)
                        .eq("config_key", key));
        return entity != null ? entity.getConfigValue() : defaultValue;
    }
}
