package com.vela.im.service.group.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: ImGroupAnnouncementEntity</p>
 * <p>Description: 群公告领域实体，映射 vela_group_announcement 表</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Data
@TableName("vela_group_announcement")
public class ImGroupAnnouncementEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应用ID */
    private Integer appId;

    /** 群组ID */
    private String groupId;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 发布者用户ID */
    private String publisherId;

    /** 发布时间戳（毫秒） */
    private Long publishTime;

    /** 更新时间戳（毫秒） */
    private Long updateTime;

    /** 逻辑删除：0-正常，1-删除 */
    private Integer delFlag;
}
