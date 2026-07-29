package com.vela.im.service.group.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.group.domain.entity.ImGroupFileEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupFileMapper;
import com.vela.im.shared.base.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupFileService {

    private final ImGroupFileMapper fileMapper;

    public GroupFileService(ImGroupFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    public Result<ImGroupFileEntity> addFile(Integer appId, String groupId, String uploaderId,
                                              String fileName, String fileUrl, Long fileSize, String fileType) {
        ImGroupFileEntity file = new ImGroupFileEntity();
        file.setAppId(appId);
        file.setGroupId(groupId);
        file.setUploaderId(uploaderId);
        file.setFileName(fileName);
        file.setFileUrl(fileUrl);
        file.setFileSize(fileSize);
        file.setFileType(fileType);
        file.setCreateTime(System.currentTimeMillis());
        fileMapper.insert(file);
        return Result.ok(file);
    }

    public Result<List<ImGroupFileEntity>> listFiles(String groupId, Integer appId) {
        QueryWrapper<ImGroupFileEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId).eq("app_id", appId).orderByDesc("create_time");
        return Result.ok(fileMapper.selectList(query));
    }

    @Transactional
    public Result<Void> deleteFile(Long fileId) {
        fileMapper.deleteById(fileId);
        return Result.ok();
    }
}
