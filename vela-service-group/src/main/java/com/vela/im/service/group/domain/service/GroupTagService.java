package com.vela.im.service.group.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.group.domain.entity.ImGroupTagEntity;
import com.vela.im.service.group.domain.entity.ImGroupTagMappingEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupTagMapper;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupTagMappingMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupTagService {

    private final ImGroupTagMapper tagMapper;
    private final ImGroupTagMappingMapper mappingMapper;

    public GroupTagService(ImGroupTagMapper tagMapper, ImGroupTagMappingMapper mappingMapper) {
        this.tagMapper = tagMapper;
        this.mappingMapper = mappingMapper;
    }

    public Result<ImGroupTagEntity> createTag(Integer appId, String userId, String name, String color) {
        ImGroupTagEntity tag = new ImGroupTagEntity();
        tag.setAppId(appId);
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color);
        tag.setCreateTime(System.currentTimeMillis());
        tagMapper.insert(tag);
        return Result.ok(tag);
    }

    public Result<List<ImGroupTagEntity>> listTags(String userId, Integer appId) {
        QueryWrapper<ImGroupTagEntity> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("app_id", appId).orderByAsc("create_time");
        return Result.ok(tagMapper.selectList(query));
    }

    @Transactional
    public Result<Void> tagGroup(Long tagId, String groupId) {
        QueryWrapper<ImGroupTagMappingEntity> check = new QueryWrapper<>();
        check.eq("tag_id", tagId).eq("group_id", groupId);
        if (mappingMapper.selectCount(check) > 0) return Result.ok();

        ImGroupTagMappingEntity mapping = new ImGroupTagMappingEntity();
        mapping.setTagId(tagId);
        mapping.setGroupId(groupId);
        mappingMapper.insert(mapping);
        return Result.ok();
    }

    @Transactional
    public Result<Void> untagGroup(Long tagId, String groupId) {
        mappingMapper.delete(new QueryWrapper<ImGroupTagMappingEntity>()
                .eq("tag_id", tagId).eq("group_id", groupId));
        return Result.ok();
    }

    public Result<List<String>> getGroupTags(Long tagId) {
        List<ImGroupTagMappingEntity> mappings = mappingMapper.selectList(
                new QueryWrapper<ImGroupTagMappingEntity>().eq("tag_id", tagId));
        List<String> groupIds = mappings.stream().map(ImGroupTagMappingEntity::getGroupId).toList();
        return Result.ok(groupIds);
    }

    @Transactional
    public Result<Void> deleteTag(Long tagId) {
        tagMapper.deleteById(tagId);
        mappingMapper.delete(new QueryWrapper<ImGroupTagMappingEntity>().eq("tag_id", tagId));
        return Result.ok();
    }
}
