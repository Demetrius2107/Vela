package com.vela.im.shared.types.message;


import com.vela.im.shared.types.ClientInfo;
import lombok.Data;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2025-03-03
 * @updateTime 2026-07-19
 *
 * Copyright © 2026 wanqiu All rights reserved
 */
@Data
public class MessageContent extends ClientInfo {

    private String messageId;

    private String fromId;

    private String toId;

    /** 消息体 */
    private String messageBody;

    /** 消息时间戳（毫秒） */
    private Long messageTime;

    /** 扩展字段（JSON） */
    private String extra;

    /** messageBodyId，消息体在 DB 中的主键 */
    private Long messageKey;

    /** 消息序列号，用于离线拉取和排序 */
    private long messageSequence;

    // ====== 引用回复 ======

    /** 被回复消息的 messageKey（为空则不是回复消息） */
    private Long replyToMsgKey;

    /** 被回复消息的发送方 ID */
    private String replyToFromId;

    /** 被回复消息的正文预览（截取前 N 个字符） */
    private String replyToMsgBody;

    // ====== 消息转发 ======

    /** 被转发消息的 messageKey（为空则不是转发消息） */
    private Long forwardFromMsgKey;

    /** 被转发消息的发送方 ID */
    private String forwardFromId;

    /** 被转发消息的正文 */
    private String forwardFromMsgBody;

    // ====== @提及 ======

    /** @提及的用户 ID 列表（JSON 数组字符串，如 "['user1','user2']"） */
    private String mentionUserIds;

    /** 是否 @所有人 */
    private boolean mentionAll;

}
