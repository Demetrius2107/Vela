package com.vela.im.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: ImStickerEntity</p>
 * <p>Description: 贴纸实体，映射 vela_sticker 表</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Data
@TableName("vela_sticker")
public class ImStickerEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应用ID */
    private Integer appId;

    /** 所属贴纸包ID */
    private Long packId;

    /** 贴纸名称 */
    private String name;

    /** 贴纸文件URL */
    private String fileUrl;

    /** 宽度（像素） */
    private Integer width;

    /** 高度（像素） */
    private Integer height;

    /** 排序号 */
    private Integer sortOrder;

    private Long createTime;
}
