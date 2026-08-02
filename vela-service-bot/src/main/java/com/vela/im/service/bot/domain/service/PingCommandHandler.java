package com.vela.im.service.bot.domain.service;

import com.vela.im.shared.types.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PingCommandHandler implements BotCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(PingCommandHandler.class);

    public PingCommandHandler(BotCommandRegistry registry) {
        registry.register(this);
    }

    @Override
    public String command() { return "ping"; }

    @Override
    public String handle(MessageContent original, String[] args) {
        return "pong! 🏓 (" + System.currentTimeMillis() + ")";
    }
}
