package com.vela.im.service.message.domain.strategy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.message.domain.entity.ImMessageBodyEntity;
import com.vela.im.shared.types.message.RecallMessageContent;
import com.vela.im.codec.pack.message.RecallMessageNotifyPack;

/**
 * <p>Title: RecallStrategy</p>
 * <p>Description: 消息撤回策略接口，不同的会话类型（单聊/群聊）有不同的撤回后处理逻辑。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
public interface RecallStrategy {

    /**
     * 执行消息撤回，包括离线队列写入、通知发送等。
     *
     * @param content   撤回请求内容
     * @param pack      撤回通知包
     * @param body      已标记删除的消息体实体
     */
    void recall(RecallMessageContent content, RecallMessageNotifyPack pack, ImMessageBodyEntity body);
}
