package com.vela.im.service.group.interfaces.rest;

import com.vela.im.service.group.domain.entity.ImGroupFileEntity;
import com.vela.im.service.group.domain.service.GroupFileService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/group/file")
public class GroupFileController {

    private final GroupFileService groupFileService;

    public GroupFileController(GroupFileService groupFileService) {
        this.groupFileService = groupFileService;
    }

    @PostMapping("/add")
    public Result<ImGroupFileEntity> addFile(@RequestParam Integer appId, @RequestParam String groupId,
                                              @RequestParam String uploaderId, @RequestParam String fileName,
                                              @RequestParam String fileUrl, @RequestParam Long fileSize,
                                              @RequestParam String fileType) {
        return groupFileService.addFile(appId, groupId, uploaderId, fileName, fileUrl, fileSize, fileType);
    }

    @GetMapping("/list")
    public Result<List<ImGroupFileEntity>> listFiles(@RequestParam String groupId, @RequestParam Integer appId) {
        return groupFileService.listFiles(groupId, appId);
    }

    @PostMapping("/delete")
    public Result<Void> deleteFile(@RequestParam Long fileId) {
        return groupFileService.deleteFile(fileId);
    }
}
