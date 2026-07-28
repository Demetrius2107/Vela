package com.vela.im.service.knowledge.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vela.im.service.knowledge.domain.entity.DocumentEntity;
import com.vela.im.service.knowledge.infrastructure.persistence.mapper.DocumentMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class DocumentService {

    private final DocumentMapper mapper;

    public DocumentService(DocumentMapper mapper) { this.mapper = mapper; }

    public Result<DocumentEntity> create(DocumentEntity entity) {
        entity.setCreateTime(System.currentTimeMillis());
        entity.setUpdateTime(entity.getCreateTime());
        mapper.insert(entity);
        return Result.ok(entity);
    }

    public Result<Map<String, Object>> list(Integer appId, String keyword, int page, int size) {
        QueryWrapper<DocumentEntity> q = new QueryWrapper<>();
        q.eq("app_id", appId);
        if (keyword != null && !keyword.isEmpty())
            q.like("title", keyword).or().like("content", keyword);
        q.orderByDesc("update_time");
        IPage<DocumentEntity> p = mapper.selectPage(new Page<>(page + 1, size), q);
        Map<String, Object> r = new HashMap<>();
        r.put("list", p.getRecords()); r.put("total", p.getTotal());
        return Result.ok(r);
    }

    public Result<DocumentEntity> get(Long id) {
        DocumentEntity e = mapper.selectById(id);
        if (e == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.DOCUMENT_NOT_FOUND);
        return Result.ok(e);
    }

    public Result<Void> update(DocumentEntity entity) {
        DocumentEntity e = mapper.selectById(entity.getId());
        if (e == null) return Result.fail(com.vela.im.shared.types.enums.BusinessErrorCode.DOCUMENT_NOT_FOUND);
        if (entity.getTitle() != null) e.setTitle(entity.getTitle());
        if (entity.getContent() != null) e.setContent(entity.getContent());
        if (entity.getSummary() != null) e.setSummary(entity.getSummary());
        if (entity.getTags() != null) e.setTags(entity.getTags());
        e.setUpdateTime(System.currentTimeMillis());
        mapper.updateById(e);
        return Result.ok();
    }

    public Result<Void> delete(Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
