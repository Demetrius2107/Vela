package com.vela.im.service.message.domain.utils;

import com.vela.im.service.message.domain.entity.ImFileEntity;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImFileMapper;
import com.vela.im.shared.config.ImServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * <p>Title: FileStorageService</p>
 * <p>Description: 文件存储服务，将上传文件保存到本地文件系统，
 * 记录文件元数据到 DB。后续可替换为 MinIO / OSS 实现。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final ImFileMapper fileMapper;
    private final ImServerProperties.FileConfig fileConfig;
    private final Path uploadDir;

    public FileStorageService(ImFileMapper fileMapper, ImServerProperties appConfig) {
        this.fileMapper = fileMapper;
        this.fileConfig = appConfig.getFile();
        this.uploadDir = Paths.get(fileConfig.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + uploadDir, e);
        }
    }

    /**
     * 保存上传文件，记录元数据并返回 ImFileEntity。
     */
    public ImFileEntity store(MultipartFile file, Integer appId, String uploaderId, String fileType) {
        // 校验文件大小
        long maxSize = "image".equals(fileType) ? fileConfig.getMaxImageSize() : fileConfig.getMaxFileSize();
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File too large: " + file.getSize() + " > " + maxSize);
        }

        // 校验文件扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        if ("image".equals(fileType) && !ext.isEmpty()) {
            List<String> allowed = Arrays.asList(fileConfig.getImageExtensions().split(","));
            if (!allowed.contains(ext)) {
                throw new IllegalArgumentException("Unsupported image format: " + ext);
            }
        }

        // 生成存储文件名
        String storageName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String dateDir = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date());
        Path targetPath = uploadDir.resolve(dateDir).resolve(storageName);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalName, e);
        }

        // 记录文件元数据
        ImFileEntity entity = new ImFileEntity();
        entity.setAppId(appId);
        entity.setUploaderId(uploaderId);
        entity.setOriginalName(originalName != null ? originalName : "unknown");
        entity.setStorageName(storageName);
        entity.setFilePath(targetPath.toString());
        entity.setFileUrl("/uploads/" + dateDir + "/" + storageName);
        entity.setFileType(fileType);
        entity.setMimeType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setCreateTime(System.currentTimeMillis());
        fileMapper.insert(entity);

        log.info("File stored: id={}, name={}, size={}, type={}", entity.getId(), originalName, file.getSize(), fileType);
        return entity;
    }

    /** 删除文件（物理文件 + DB 记录） */
    public void delete(Long fileId) {
        ImFileEntity entity = fileMapper.selectById(fileId);
        if (entity == null) return;
        try {
            Path path = Paths.get(entity.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete physical file: id={}, path={}", fileId, entity.getFilePath());
        }
        fileMapper.deleteById(fileId);
    }
}
