package com.vela.im.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: ImMessageFavoriteEntity</p>
 * <p>Description: 消息收藏实体，记录用户收藏的消息。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Data
@TableName("vela_message_favorite")
public class ImMessageFavoriteEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer appId;
    private String userId;
    /** 消息所属会话ID */
    private String conversationId;
    /** 消息发送者ID */
    private String fromId;
    /** 消息内容 */
    private String content;
    /** 消息时间戳 */
    private Long messageTime;
    /** 收藏时间 */
    private Long createTime;
}
