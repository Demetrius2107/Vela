package com.vela.im.service.admin.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.admin.domain.SystemConfigEntity;
import com.vela.im.service.admin.infrastructure.persistence.mapper.SystemConfigMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigService {

    private final SystemConfigMapper configMapper;

    public SystemConfigService(SystemConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @PostConstruct
    public void initDefaults() {
        setIfAbsent("message.recallTimeout", "120000", "消息可撤回时间(毫秒)");
        setIfAbsent("message.maxSize", "65536", "消息体最大字节数");
        setIfAbsent("message.rateLimit", "20", "每秒消息发送上限");
        setIfAbsent("user.registerEnabled", "true", "是否允许用户注册");
        setIfAbsent("group.maxMemberCount", "500", "群成员上限");
        setIfAbsent("offline.messageCount", "1000", "离线消息存储条数");
    }

    private void setIfAbsent(String key, String value, String desc) {
        if (configMapper.selectCount(new QueryWrapper<SystemConfigEntity>().eq("config_key", key)) == 0) {
            SystemConfigEntity entity = new SystemConfigEntity();
            entity.setConfigKey(key); entity.setConfigValue(value);
            entity.setDescription(desc); entity.setUpdateTime(System.currentTimeMillis());
            configMapper.insert(entity);
        }
    }

    public Result<List<SystemConfigEntity>> listConfigs() {
        return Result.ok(configMapper.selectList(new QueryWrapper<SystemConfigEntity>().orderByAsc("config_key")));
    }

    @Transactional
    public Result<Void> updateConfig(Long id, String value) {
        SystemConfigEntity config = configMapper.selectById(id);
        if (config == null) return Result.fail(500, "配置项不存在");
        config.setConfigValue(value);
        config.setUpdateTime(System.currentTimeMillis());
        configMapper.updateById(config);
        return Result.ok();
    }

    public String getConfig(String key, String defaultValue) {
        SystemConfigEntity config = configMapper.selectOne(
                new QueryWrapper<SystemConfigEntity>().eq("config_key", key));
        return config != null ? config.getConfigValue() : defaultValue;
    }
}
