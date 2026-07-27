package com.vela.im.shared.types.message;

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
public class OfflineMessageContent {

    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private Long messageTime;

    private String extra;

    private Integer delFlag;

    private String fromId;

    private String toId;

    /** 序列号*/
    private Long messageSequence;

    private String messageRandom;

    private Integer conversationType;

    private String conversationId;

    // ====== 引用回复 ======

    /** 被回复消息的 messageKey */
    private Long replyToMsgKey;

    /** 被回复消息的发送方 ID */
    private String replyToFromId;

    /** 被回复消息的正文预览 */
    private String replyToMsgBody;

    // ====== 消息转发 ======

    /** 被转发消息的 messageKey */
    private Long forwardFromMsgKey;

    /** 被转发消息的发送方 ID */
    private String forwardFromId;

    /** 被转发消息的正文 */
    private String forwardFromMsgBody;

    // ====== @提及 ======

    /** @提及的用户 ID 列表 */
    private String mentionUserIds;

    /** 是否 @所有人 */
    private boolean mentionAll;

    // ====== 贴纸消息 ======

    /** 贴纸 ID */
    private String stickerId;

}
