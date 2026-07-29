package com.vela.im.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: ImStickerPackEntity</p>
 * <p>Description: 贴纸包实体，映射 vela_sticker_pack 表</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Data
@TableName("vela_sticker_pack")
public class ImStickerPackEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应用ID */
    private Integer appId;

    /** 贴纸包名称 */
    private String name;

    /** 封面图标URL */
    private String icon;

    /** 排序号 */
    private Integer sortOrder;

    /** 是否系统内置（0-用户自定义，1-系统预置） */
    private Integer systemFlag;

    private Long createTime;

    private Long updateTime;
}
