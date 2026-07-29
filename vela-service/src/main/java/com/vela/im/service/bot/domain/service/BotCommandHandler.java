package com.vela.im.service.bot.domain.service;

import com.vela.im.shared.types.message.MessageContent;

/**
 * Bot 指令处理器接口。实现此接口处理特定的斜杠命令。
 */
public interface BotCommandHandler {

    /** 指令名称（不含斜杠，如 "help"、"weather"） */
    String command();

    /** 处理指令并返回回复文本 */
    String handle(MessageContent original, String[] args);
}
