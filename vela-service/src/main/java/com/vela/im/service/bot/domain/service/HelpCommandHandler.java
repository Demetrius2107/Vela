package com.vela.im.service.bot.domain.service;

import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements BotCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(HelpCommandHandler.class);

    private final BotCommandRegistry registry;

    public HelpCommandHandler(BotCommandRegistry registry) {
        this.registry = registry;
        this.registry.register(this);
    }

    @Override
    public String command() { return "help"; }

    @Override
    public String handle(MessageContent original, String[] args) {
        StringBuilder sb = new StringBuilder("📋 可用指令：\n");
        java.util.Map<String, String> commands = registry.listCommands();
        for (java.util.Map.Entry<String, String> entry : commands.entrySet()) {
            sb.append(entry.getKey()).append(" — ").append(entry.getValue()).append("\n");
        }
        sb.append("\n发送 /help 查看此列表");
        return sb.toString();
    }
}
