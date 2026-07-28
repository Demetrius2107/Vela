package com.vela.im.service.office.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vela.im.service.office.domain.entity.ScheduleEntity;
import com.vela.im.service.office.infrastructure.persistence.mapper.ScheduleMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class ScheduleService {

    private final ScheduleMapper mapper;

    public ScheduleService(ScheduleMapper mapper) { this.mapper = mapper; }

    public Result<ScheduleEntity> create(ScheduleEntity entity) {
        entity.setCreateTime(System.currentTimeMillis());
        entity.setUpdateTime(entity.getCreateTime());
        mapper.insert(entity);
        return Result.ok(entity);
    }

    public Result<Map<String, Object>> list(String userId, Integer appId, int page, int size) {
        QueryWrapper<ScheduleEntity> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("app_id", appId).orderByDesc("start_time");
        IPage<ScheduleEntity> p = mapper.selectPage(new Page<>(page + 1, size), q);
        Map<String, Object> r = new HashMap<>();
        r.put("list", p.getRecords()); r.put("total", p.getTotal());
        return Result.ok(r);
    }

    public Result<Void> updateStatus(Long id, Integer status) {
        ScheduleEntity e = mapper.selectById(id);
        if (e == null) return Result.fail(500, "日程不存在");
        e.setStatus(status); e.setUpdateTime(System.currentTimeMillis());
        mapper.updateById(e);
        return Result.ok();
    }

    public Result<Void> delete(Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
