package com.vela.im.service.bot.domain.service;

import com.vela.im.service.bot.domain.entity.ImBotEntity;
import com.vela.im.service.bot.infrastructure.persistence.mapper.ImBotMapper;
import com.vela.im.shared.base.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotService - Bot 管理服务")
class BotServiceTest {

    @Mock
    private ImBotMapper botMapper;

    private BotService service;

    @BeforeEach
    void setUp() {
        service = new BotService(botMapper);
    }

    @Nested
    @DisplayName("register - 注册 Bot")
    class RegisterTest {
        @Test
        @DisplayName("注册成功应返回 Bot 实体并写入 DB")
        void successfulRegister() {
            Result<ImBotEntity> result = service.register(100, "bot1", "TestBot",
                    "https://hook.example.com", "测试Bot");

            assertTrue(result.isOk());
            assertEquals("bot1", result.getData().getBotId());
            assertEquals(1, result.getData().getStatus().intValue());
            assertNotNull(result.getData().getApiKey());
            verify(botMapper, times(1)).insert(any(ImBotEntity.class));
        }
    }

    @Nested
    @DisplayName("toggleStatus - 切换状态")
    class ToggleStatusTest {
        @Test
        @DisplayName("不存在的 Bot 应返回错误")
        void nonExistentBot() {
            when(botMapper.selectById(999L)).thenReturn(null);

            Result<Void> result = service.toggleStatus(999L);

            assertFalse(result.isOk());
        }

        @Test
        @DisplayName("存在应切换启用/禁用状态")
        void toggleExistingBot() {
            ImBotEntity bot = new ImBotEntity();
            bot.setId(1L);
            bot.setStatus(1);
            when(botMapper.selectById(1L)).thenReturn(bot);

            service.toggleStatus(1L);

            assertEquals(0, bot.getStatus().intValue());
            verify(botMapper, times(1)).updateById(bot);
        }
    }

    @Nested
    @DisplayName("regenerateApiKey - 重置 API 密钥")
    class RegenerateKeyTest {
        @Test
        @DisplayName("成功应返回新密钥")
        void regeneratesKey() {
            ImBotEntity bot = new ImBotEntity();
            bot.setId(1L);
            bot.setApiKey("old-key");
            when(botMapper.selectById(1L)).thenReturn(bot);

            Result<String> result = service.regenerateApiKey(1L);

            assertTrue(result.isOk());
            assertNotNull(result.getData());
            assertNotEquals("old-key", result.getData());
        }
    }

    @Nested
    @DisplayName("list - 列表查询")
    class ListTest {
        @Test
        @DisplayName("应返回列表")
        void returnsList() {
            Result<List<ImBotEntity>> result = service.list(100);

            assertTrue(result.isOk());
        }
    }
}
