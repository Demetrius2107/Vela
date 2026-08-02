package com.vela.im.service.group.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: ImGroupAnnouncementReadEntity</p>
 * <p>Description: 群公告已读记录实体，映射 vela_group_announcement_read 表</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Data
@TableName("vela_group_announcement_read")
public class ImGroupAnnouncementReadEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告ID */
    private Long announcementId;

    /** 应用ID */
    private Integer appId;

    /** 群组ID */
    private String groupId;

    /** 已读成员ID */
    private String memberId;

    /** 已读时间戳（毫秒） */
    private Long readTime;
}
