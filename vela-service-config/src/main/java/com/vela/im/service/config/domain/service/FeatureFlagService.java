package com.vela.im.service.config.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vela.im.service.config.domain.entity.FeatureFlagEntity;
import com.vela.im.service.config.infrastructure.persistence.mapper.FeatureFlagMapper;
import com.vela.im.shared.base.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: FeatureFlagService</p>
 * <p>Description: 功能开关服务，支持本地缓存+数据库双写，客户端启动时拉取。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Service
public class FeatureFlagService {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagService.class);
    private final FeatureFlagMapper flagMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 本地缓存：flagKey → FeatureFlagEntity */
    private final Map<String, FeatureFlagEntity> cache = new ConcurrentHashMap<>();

    public FeatureFlagService(FeatureFlagMapper flagMapper) {
        this.flagMapper = flagMapper;
    }

    @PostConstruct
    public void initDefaults() {
        setIfAbsent("bot_market", "Bot市场", 1, "Bot发现与安装");
        setIfAbsent("ai_assistant", "AI助手", 0, "智能对话助手");
        setIfAbsent("video_call", "视频通话", 1, "WebRTC视频通话");
        setIfAbsent("audio_call", "语音通话", 1, "WebRTC语音通话");
        setIfAbsent("office_suite", "办公生态", 1, "日程/待办/审批");
        setIfAbsent("knowledge_base", "知识库", 1, "文档管理");
        refreshCache();
    }

    private void setIfAbsent(String key, String name, int enabled, String desc) {
        if (flagMapper.selectCount(new QueryWrapper<FeatureFlagEntity>().eq("flag_key", key)) == 0) {
            FeatureFlagEntity entity = new FeatureFlagEntity();
            entity.setAppId(1);
            entity.setFlagKey(key);
            entity.setFlagName(name);
            entity.setEnabled(enabled);
            entity.setDescription(desc);
            entity.setUpdateTime(System.currentTimeMillis());
            flagMapper.insert(entity);
        }
    }

    /** 刷新本地缓存 */
    public void refreshCache() {
        List<FeatureFlagEntity> all = flagMapper.selectList(new QueryWrapper<>());
        cache.clear();
        for (FeatureFlagEntity f : all) {
            cache.put(f.getFlagKey(), f);
        }
        log.info("FeatureFlag cache refreshed: {} items", all.size());
    }

    /** 获取所有功能开关（供管理后台使用） */
    public Result<List<FeatureFlagEntity>> listAll() {
        return Result.ok(new ArrayList<>(cache.values()));
    }

    /** 客户端拉取功能开关列表（返回各端需要的格式） */
    public Result<Map<String, Boolean>> getClientFlags(Integer appId, String userId) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (FeatureFlagEntity f : cache.values()) {
            if (f.getEnabled() == 1) {
                result.put(f.getFlagKey(), true);
            } else {
                // 关闭时检查白名单
                boolean whitelisted = isWhitelisted(f, userId);
                result.put(f.getFlagKey(), whitelisted);
            }
        }
        return Result.ok(result);
    }

    /** 检查用户是否有权限使用某功能 */
    public boolean isEnabled(String flagKey, String userId) {
        FeatureFlagEntity f = cache.get(flagKey);
        if (f == null) return false;
        if (f.getEnabled() == 1) return true;
        return isWhitelisted(f, userId);
    }

    private boolean isWhitelisted(FeatureFlagEntity f, String userId) {
        if (f.getUserWhitelist() == null || f.getUserWhitelist().isEmpty()) return false;
        try {
            List<String> whitelist = objectMapper.readValue(f.getUserWhitelist(),
                    new TypeReference<List<String>>() {});
            return whitelist.contains(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /** 更新功能开关 */
    public Result<Void> update(Long id, Integer enabled, String userWhitelist) {
        FeatureFlagEntity f = flagMapper.selectById(id);
        if (f == null) return Result.fail(404, "开关不存在");
        if (enabled != null) f.setEnabled(enabled);
        if (userWhitelist != null) f.setUserWhitelist(userWhitelist);
        f.setUpdateTime(System.currentTimeMillis());
        flagMapper.updateById(f);
        refreshCache();
        return Result.ok();
    }
}
