package com.vela.im.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 消息已读记录，跟踪每个成员对每条消息的已读状态。
 */
@Data
@TableName("vela_message_read")
public class ImMessageReadEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;

    /** 群组ID（单聊为空） */
    private String groupId;

    /** 消息Key */
    private Long messageKey;

    /** 已读成员ID */
    private String memberId;

    /** 已读时间戳 */
    private Long readTime;
}
