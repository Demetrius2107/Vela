package com.vela.im.service.application.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Title: RetryUtilTest</p>
 * <p>Description: RetryUtil 指数退避重试工具类单元测试</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@DisplayName("RetryUtil - 指数退避重试工具类")
class RetryUtilTest {

    @Nested
    @DisplayName("retryWithExponentialBackoff - 指数退避重试（Supplier）")
    class RetryTest {

        @Test
        @DisplayName("首次成功应不重试")
        void firstAttemptSuccess() {
            AtomicInteger counter = new AtomicInteger(0);
            boolean result = RetryUtil.retryWithExponentialBackoff(() -> {
                counter.incrementAndGet();
                return true;
            }, 3, 10L, 100L, "test-task");
            assertTrue(result);
            assertEquals(1, counter.get());
        }

        @Test
        @DisplayName("第一次失败第二次成功应重试1次")
        void secondAttemptSucceeds() {
            AtomicInteger counter = new AtomicInteger(0);
            boolean result = RetryUtil.retryWithExponentialBackoff(() -> {
                counter.incrementAndGet();
                if (counter.get() == 1) {
                    throw new RuntimeException("first attempt fails");
                }
                return true;
            }, 3, 10L, 100L, "test-task");
            assertTrue(result);
            assertEquals(2, counter.get());
        }

        @Test
        @DisplayName("所有重试都失败应返回 false")
        void allAttemptsFail() {
            AtomicInteger counter = new AtomicInteger(0);
            boolean result = RetryUtil.retryWithExponentialBackoff(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("always fails");
            }, 3, 10L, 100L, "test-task");
            assertFalse(result);
            assertEquals(3, counter.get());
        }

        @Test
        @DisplayName("业务返回 false 不应重试")
        void businessFailureNotRetried() {
            AtomicInteger counter = new AtomicInteger(0);
            boolean result = RetryUtil.retryWithExponentialBackoff(() -> {
                counter.incrementAndGet();
                return false; // business failure, not exception
            }, 3, 10L, 100L, "test-task");
            assertFalse(result);
            assertEquals(1, counter.get()); // only attempted once
        }

        @Test
        @DisplayName("使用默认参数应正常执行")
        void defaultParametersWork() {
            boolean result = RetryUtil.retryWithExponentialBackoff(() -> true, "test-default");
            assertTrue(result);
        }

        @Test
        @DisplayName("重试1次时最多执行1次")
        void zeroRetriesExecutesOnce() {
            AtomicInteger counter = new AtomicInteger(0);
            boolean result = RetryUtil.retryWithExponentialBackoff(() -> {
                counter.incrementAndGet();
                throw new RuntimeException("fail");
            }, 1, 10L, 100L, "test-once");
            assertFalse(result);
            assertEquals(1, counter.get());
        }
    }

    @Nested
    @DisplayName("retryRunnableWithExponentialBackoff - Runnable 版本")
    class RetryRunnableTest {

        @Test
        @DisplayName("Runnable 成功应返回 true")
        void runnableSuccess() {
            AtomicInteger counter = new AtomicInteger(0);
            boolean result = RetryUtil.retryRunnableWithExponentialBackoff(
                    counter::incrementAndGet, 3, 10L, 100L, "runnable-test");
            assertTrue(result);
            assertEquals(1, counter.get());
        }

        @Test
        @DisplayName("Runnable 持续抛异常应返回 false")
        void runnableAlwaysThrows() {
            boolean result = RetryUtil.retryRunnableWithExponentialBackoff(() -> {
                throw new RuntimeException("always fail");
            }, 2, 10L, 100L, "runnable-fail");
            assertFalse(result);
        }
    }
}
