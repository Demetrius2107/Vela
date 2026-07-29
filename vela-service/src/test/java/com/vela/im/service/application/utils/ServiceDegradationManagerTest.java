package com.vela.im.service.application.utils;

import com.vela.im.shared.config.ImServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceDegradationManager - 服务降级管理")
class ServiceDegradationManagerTest {

    @Mock
    private ImServerProperties appConfig;
    @Mock
    private StringRedisTemplate stringRedisTemplate;

    private ServiceDegradationManager manager;

    @BeforeEach
    void setUp() {
        ImServerProperties.RetryConfig retryConfig = new ImServerProperties.RetryConfig();
        retryConfig.setMaxRetries(3);
        lenient().when(appConfig.getRetry()).thenReturn(retryConfig);
        manager = new ServiceDegradationManager(appConfig, stringRedisTemplate);
    }

    @Nested
    @DisplayName("Redis 健康状态")
    class RedisHealthTest {
        @Test
        @DisplayName("初始状态应健康")
        void initiallyHealthy() {
            assertTrue(manager.isRedisAvailable());
        }

        @Test
        @DisplayName("连续失败3次后应降级")
        void threeFailuresDegrades() {
            manager.reportRedisFailure("test1");
            assertTrue(manager.isRedisAvailable());

            manager.reportRedisFailure("test2");
            assertTrue(manager.isRedisAvailable());

            manager.reportRedisFailure("test3");
            assertFalse(manager.isRedisAvailable());
        }

        @Test
        @DisplayName("成功报告应恢复健康")
        void successRecovers() {
            manager.reportRedisFailure("fail1");
            manager.reportRedisFailure("fail2");
            manager.reportRedisFailure("fail3");
            assertFalse(manager.isRedisAvailable());

            manager.reportRedisSuccess();
            assertTrue(manager.isRedisAvailable());
        }
    }

    @Nested
    @DisplayName("MQ 健康状态")
    class MqHealthTest {
        @Test
        @DisplayName("状态字符串应包含组件信息")
        void statusString() {
            String status = manager.getStatus();
            assertTrue(status.contains("Redis"));
            assertTrue(status.contains("MQ"));
        }
    }
}
