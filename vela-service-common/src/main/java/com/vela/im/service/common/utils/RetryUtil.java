package com.vela.im.service.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * <p>Title: RetryUtil</p>
 * <p>Description: 指数退避重试工具类，支持可配置的重试次数和延迟策略。
 * 延迟策略：baseDelayMs × 2^(attempt-1)，上限 maxDelayMs。
 * 适用于网络抖动、临时性故障等可恢复异常场景。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
public class RetryUtil {

    private static final Logger log = LoggerFactory.getLogger(RetryUtil.class);

    /** 默认最大重试次数 */
    public static final int DEFAULT_MAX_RETRIES = 3;

    /** 默认初始延迟（毫秒） */
    public static final long DEFAULT_BASE_DELAY_MS = 100L;

    /** 默认最大延迟（毫秒） */
    public static final long DEFAULT_MAX_DELAY_MS = 2000L;

    private RetryUtil() {
        // utility class
    }

    /**
     * 执行带指数退避的重试。
     *
     * @param task       重试任务，返回 true 表示成功
     * @param maxRetries 最大重试次数（含首次尝试）
     * @param baseDelayMs 初始延迟（毫秒）
     * @param maxDelayMs  最大延迟（毫秒）
     * @param taskName   任务名称（用于日志）
     * @return true=成功, false=所有重试均失败
     */
    public static boolean retryWithExponentialBackoff(
            Supplier<Boolean> task, int maxRetries, long baseDelayMs, long maxDelayMs, String taskName) {

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                if (task.get()) {
                    if (attempt > 0) {
                        log.info("{} succeeded on attempt {}/{}", taskName, attempt + 1, maxRetries);
                    }
                    return true;
                }
                // task returned false (business failure) — not a retryable exception
                log.warn("{} returned false on attempt {}/{}, not retrying", taskName, attempt + 1, maxRetries);
                return false;
            } catch (Exception e) {
                boolean isLastAttempt = (attempt == maxRetries - 1);
                if (isLastAttempt) {
                    log.error("{} exhausted after {}/{} attempts, error={}",
                            taskName, attempt + 1, maxRetries, e.getMessage(), e);
                    return false;
                }
                long delay = calculateDelay(attempt, baseDelayMs, maxDelayMs);
                log.warn("{} failed on attempt {}/{}, retrying in {}ms, error={}",
                        taskName, attempt + 1, maxRetries, delay, e.getMessage());
                sleep(delay);
            }
        }
        return false;
    }

    /**
     * 使用默认参数执行指数退避重试。
     *
     * @param task     重试任务
     * @param taskName 任务名称（用于日志）
     * @return true=成功, false=所有重试均失败
     */
    public static boolean retryWithExponentialBackoff(Supplier<Boolean> task, String taskName) {
        return retryWithExponentialBackoff(task, DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY_MS, DEFAULT_MAX_DELAY_MS, taskName);
    }

    /**
     * 执行带指数退避的重试（Runnable 版本，需要自己处理返回值）。
     *
     * @param task       重试任务
     * @param maxRetries 最大重试次数
     * @param baseDelayMs 初始延迟（毫秒）
     * @param maxDelayMs  最大延迟（毫秒）
     * @param taskName   任务名称
     * @return true=成功（任务未抛出异常）, false=所有重试均失败
     */
    public static boolean retryRunnableWithExponentialBackoff(
            Runnable task, int maxRetries, long baseDelayMs, long maxDelayMs, String taskName) {
        return retryWithExponentialBackoff(() -> {
            task.run();
            return true;
        }, maxRetries, baseDelayMs, maxDelayMs, taskName);
    }

    /**
     * 计算第 attempt 次重试的延迟时间。
     * 公式：baseDelayMs × 2^attempt，上限 maxDelayMs。
     */
    private static long calculateDelay(int attempt, long baseDelayMs, long maxDelayMs) {
        long delay = baseDelayMs * (1L << attempt);
        return Math.min(delay, maxDelayMs);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
