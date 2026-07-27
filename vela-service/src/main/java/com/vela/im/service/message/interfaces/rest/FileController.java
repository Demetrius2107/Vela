package com.vela.im.service.message.interfaces.rest;

import com.vela.im.service.application.utils.FileStorageService;
import com.vela.im.service.message.domain.entity.ImFileEntity;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/file")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload/image")
    public Result<ImFileEntity> uploadImage(@RequestParam MultipartFile file,
                                            @RequestParam Integer appId,
                                            @RequestParam String uploaderId) {
        try {
            ImFileEntity entity = fileStorageService.store(file, appId, uploaderId, "image");
            return Result.ok(entity);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/upload/file")
    public Result<ImFileEntity> uploadFile(@RequestParam MultipartFile file,
                                           @RequestParam Integer appId,
                                           @RequestParam String uploaderId) {
        try {
            ImFileEntity entity = fileStorageService.store(file, appId, uploaderId, "file");
            return Result.ok(entity);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long fileId) {
        fileStorageService.delete(fileId);
        return Result.ok();
    }
}
