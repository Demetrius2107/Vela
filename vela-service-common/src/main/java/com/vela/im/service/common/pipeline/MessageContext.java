package com.vela.im.service.common.pipeline;

import com.vela.im.shared.types.ClientInfo;
import com.vela.im.shared.types.message.MessageContent;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: MessageContext</p>
 * <p>Description: 消息处理管道上下文，在管道各节点间传递当前消息及处理状态。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-24
 */
public class MessageContext {

    /** 原始消息内容 */
    private final MessageContent messageContent;

    /** 在线客户端列表（推送结果） */
    private List<ClientInfo> onlineClients;

    /** 处理是否已被中断（如校验失败） */
    private boolean interrupted;

    /** 中断原因 */
    private Object interruptCause;

    public MessageContext(MessageContent messageContent) {
        this.messageContent = messageContent;
        this.onlineClients = new ArrayList<>();
        this.interrupted = false;
    }

    public MessageContent getMessageContent() {
        return messageContent;
    }

    public List<ClientInfo> getOnlineClients() {
        return onlineClients;
    }

    public void setOnlineClients(List<ClientInfo> onlineClients) {
        this.onlineClients = onlineClients;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void interrupt(Object cause) {
        this.interrupted = true;
        this.interruptCause = cause;
    }

    public Object getInterruptCause() {
        return interruptCause;
    }
}
