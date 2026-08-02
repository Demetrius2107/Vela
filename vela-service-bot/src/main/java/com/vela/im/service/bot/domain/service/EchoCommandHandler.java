package com.vela.im.service.bot.domain.service;

import com.vela.im.shared.types.message.MessageContent;
import org.springframework.stereotype.Component;

@Component
public class EchoCommandHandler implements BotCommandHandler {

    public EchoCommandHandler(BotCommandRegistry registry) {
        registry.register(this);
    }

    @Override
    public String command() { return "echo"; }

    @Override
    public String handle(MessageContent original, String[] args) {
        if (args.length == 0) return "请提供要回显的内容";
        return String.join(" ", args);
    }
}
