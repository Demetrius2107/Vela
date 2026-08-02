package com.vela.im.service.office.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vela.im.service.office.domain.entity.TodoEntity;
import com.vela.im.service.office.infrastructure.persistence.mapper.TodoMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class TodoService {

    private final TodoMapper mapper;

    public TodoService(TodoMapper mapper) { this.mapper = mapper; }

    public Result<TodoEntity> create(TodoEntity entity) {
        entity.setCreateTime(System.currentTimeMillis());
        entity.setUpdateTime(entity.getCreateTime());
        mapper.insert(entity);
        return Result.ok(entity);
    }

    public Result<Map<String, Object>> list(String userId, Integer appId, Integer status, int page, int size) {
        QueryWrapper<TodoEntity> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("app_id", appId);
        if (status != null) q.eq("status", status);
        q.orderByDesc("priority").orderByAsc("due_time");
        IPage<TodoEntity> p = mapper.selectPage(new Page<>(page + 1, size), q);
        Map<String, Object> r = new HashMap<>();
        r.put("list", p.getRecords()); r.put("total", p.getTotal());
        return Result.ok(r);
    }

    public Result<Void> updateStatus(Long id, Integer status) {
        TodoEntity e = mapper.selectById(id);
        if (e == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.TODO_NOT_FOUND);
        e.setStatus(status); e.setUpdateTime(System.currentTimeMillis());
        mapper.updateById(e);
        return Result.ok();
    }

    public Result<Void> delete(Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
