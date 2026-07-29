package com.vela.im.service.bot.domain.service;

import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.shared.types.message.MessageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotMessageRelayService - Bot 消息转发服务")
class BotMessageRelayServiceTest {

    @Mock
    private BotService botService;
    @Mock
    private MessageProducer messageProducer;
    @Mock
    private BotCommandRegistry commandRegistry;

    private BotMessageRelayService relayService;

    @BeforeEach
    void setUp() {
        relayService = new BotMessageRelayService(botService, messageProducer, commandRegistry);
    }

    private MessageContent createMessage(String fromId, String toId, String body) {
        MessageContent msg = new MessageContent();
        msg.setAppId(100);
        msg.setFromId(fromId);
        msg.setToId(toId);
        msg.setMessageBody(body);
        msg.setMessageTime(System.currentTimeMillis());
        return msg;
    }

    @Test
    @DisplayName("非 Bot 目标应返回 false")
    void nonBotTargetReturnsFalse() {
        when(botService.getByBotId("user1", 100)).thenReturn(null);

        boolean result = relayService.relayToBotIfNeeded(createMessage("user1", "user2", "hello"));

        assertFalse(result);
    }

    @Test
    @DisplayName("已禁用的 Bot 应返回 false")
    void disabledBotReturnsFalse() {
        ImBotEntity bot = new ImBotEntity();
        bot.setBotId("bot1");
        bot.setStatus(0);
        when(botService.getByBotId("bot1", 100)).thenReturn(bot);

        boolean result = relayService.relayToBotIfNeeded(createMessage("user1", "bot1", "hello"));

        assertFalse(result);
    }

    @Test
    @DisplayName("以斜杠开头的消息应触发指令系统")
    void commandMessageTriggersCommand() {
        ImBotEntity bot = new ImBotEntity();
        bot.setBotId("bot1");
        bot.setStatus(1);
        when(botService.getByBotId("bot1", 100)).thenReturn(bot);
        BotCommandHandler helpHandler = mock(BotCommandHandler.class);
        when(helpHandler.command()).thenReturn("help");
        when(helpHandler.handle(any(), any())).thenReturn("帮助信息");

        relayService.relayToBotIfNeeded(createMessage("user1", "bot1", "/help"));

        verify(messageProducer, timeout(1000)).sendToUser(
                eq("user1"), any(), any(), eq(100));
    }

    @Test
    @DisplayName("用 @Bot 应触发群聊处理")
    void groupMentionHandled() {
        ImBotEntity bot = new ImBotEntity();
        bot.setBotId("bot1");
        bot.setStatus(1);
        bot.setAppId(100);
        when(botService.getByBotId("bot1", 100)).thenReturn(bot);

        boolean result = relayService.handleGroupMention(
                createMessage("user1", "bot1", "帮我查天气"), "group001");

        assertTrue(result);
    }
}
