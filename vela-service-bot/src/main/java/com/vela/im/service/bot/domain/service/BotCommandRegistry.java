package com.vela.im.service.bot.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Bot 指令注册中心，管理所有斜杠命令的注册和调度。
 */
@Service
public class BotCommandRegistry {

    private static final Logger log = LoggerFactory.getLogger(BotCommandRegistry.class);
    private final Map<String, BotCommandHandler> handlers = new HashMap<>();

    public void register(BotCommandHandler handler) {
        handlers.put(handler.command().toLowerCase(), handler);
        log.info("Bot command registered: /{}", handler.command());
    }

    public BotCommandHandler get(String command) {
        return handlers.get(command.toLowerCase());
    }

    public boolean hasCommand(String command) {
        return handlers.containsKey(command.toLowerCase());
    }

    public Map<String, String> listCommands() {
        Map<String, String> result = new HashMap<>();
        for (BotCommandHandler h : handlers.values()) {
            result.put("/" + h.command(), h.getClass().getSimpleName());
        }
        return result;
    }
}
