package com.vela.im.service.office.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vela.im.service.office.domain.entity.ApprovalEntity;
import com.vela.im.service.office.infrastructure.persistence.mapper.ApprovalMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class ApprovalService {

    private final ApprovalMapper mapper;

    public ApprovalService(ApprovalMapper mapper) { this.mapper = mapper; }

    public Result<ApprovalEntity> submit(ApprovalEntity entity) {
        entity.setStatus(0);
        entity.setCreateTime(System.currentTimeMillis());
        entity.setUpdateTime(entity.getCreateTime());
        mapper.insert(entity);
        return Result.ok(entity);
    }

    public Result<Map<String, Object>> list(String userId, Integer appId, Integer status, int page, int size) {
        QueryWrapper<ApprovalEntity> q = new QueryWrapper<>();
        q.eq("app_id", appId);
        if (userId != null) q.eq("applicant_id", userId);
        if (status != null) q.eq("status", status);
        q.orderByDesc("create_time");
        IPage<ApprovalEntity> p = mapper.selectPage(new Page<>(page + 1, size), q);
        Map<String, Object> r = new HashMap<>();
        r.put("list", p.getRecords()); r.put("total", p.getTotal());
        return Result.ok(r);
    }

    public Result<Void> approve(Long id, String approverId, String comment, boolean passed) {
        ApprovalEntity e = mapper.selectById(id);
        if (e == null) return Result.fail(500, "审批不存在");
        if (e.getStatus() != 0) return Result.fail(500, "审批已处理");
        e.setStatus(passed ? 1 : 2);
        e.setApproverId(approverId);
        e.setComment(comment);
        e.setUpdateTime(System.currentTimeMillis());
        mapper.updateById(e);
        return Result.ok();
    }

    public Result<Void> delete(Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
