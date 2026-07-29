package com.vela.im.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: ImFileEntity</p>
 * <p>Description: 上传文件实体，映射 vela_file 表</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Data
@TableName("vela_file")
public class ImFileEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应用ID */
    private Integer appId;

    /** 上传者用户ID */
    private String uploaderId;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名（UUID） */
    private String storageName;

    /** 文件存储路径 */
    private String filePath;

    /** 文件访问URL */
    private String fileUrl;

    /** 文件类型：image/file/video/audio */
    private String fileType;

    /** MIME类型 */
    private String mimeType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 图片宽度（图片类型时有效） */
    private Integer imageWidth;

    /** 图片高度（图片类型时有效） */
    private Integer imageHeight;

    /** 上传时间戳 */
    private Long createTime;
}
