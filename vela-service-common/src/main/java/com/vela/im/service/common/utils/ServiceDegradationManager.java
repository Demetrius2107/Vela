package com.vela.im.service.common.utils;

import com.vela.im.shared.config.ImServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * <p>Title: ServiceDegradationManager</p>
 * <p>Description: 服务降级管理器，追踪 Redis 和 RabbitMQ 的运行健康状态。
 * 采用简易熔断模式：连续失败 N 次后标记为 DEGRADED，经过冷却期后通过探针自动恢复。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Component
public class ServiceDegradationManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceDegradationManager.class);

    public enum Service { REDIS, MQ }
    public enum State { HEALTHY, DEGRADED }

    /** 连续失败阈值（达到此次数后标记为降级） */
    private static final int DEFAULT_FAILURE_THRESHOLD = 3;

    /** 降级冷却期（毫秒），过后尝试恢复探针 */
    private static final long DEFAULT_COOLDOWN_MS = 30_000L;

    /** 恢复探针超时（毫秒） */
    private static final long PROBE_TIMEOUT_MS = 2_000L;

    private final int failureThreshold;
    private final long cooldownMs;
    private final StringRedisTemplate stringRedisTemplate;

    // Redis 状态
    private volatile State redisState = State.HEALTHY;
    private int redisConsecutiveFailures = 0;
    private volatile long redisDegradedAt = 0L;

    // MQ 状态
    private volatile State mqState = State.HEALTHY;
    private int mqConsecutiveFailures = 0;
    private volatile long mqDegradedAt = 0L;

    public ServiceDegradationManager(ImServerProperties appConfig,
                                     StringRedisTemplate stringRedisTemplate) {
        this.failureThreshold = appConfig.getRetry() != null
                ? appConfig.getRetry().getMaxRetries() : DEFAULT_FAILURE_THRESHOLD;
        this.cooldownMs = DEFAULT_COOLDOWN_MS;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // ==================== Redis ====================

    public boolean isRedisAvailable() {
        checkRecovery(Service.REDIS);
        return redisState == State.HEALTHY;
    }

    public synchronized void reportRedisFailure(String context) {
        redisConsecutiveFailures++;
        log.warn("Redis failure reported [{}] (consecutive={}/{})",
                context, redisConsecutiveFailures, failureThreshold);
        if (redisConsecutiveFailures >= failureThreshold && redisState == State.HEALTHY) {
            redisState = State.DEGRADED;
            redisDegradedAt = System.currentTimeMillis();
            log.error(">>>>> Redis degraded! Consecutive failures={}, threshold={}",
                    redisConsecutiveFailures, failureThreshold);
        }
    }

    public synchronized void reportRedisSuccess() {
        if (redisState != State.HEALTHY) {
            log.info("Redis recovered, restoring HEALTHY state");
        }
        redisConsecutiveFailures = 0;
        redisState = State.HEALTHY;
        redisDegradedAt = 0L;
    }

    // ==================== MQ ====================

    public boolean isMqAvailable() {
        checkRecovery(Service.MQ);
        return mqState == State.HEALTHY;
    }

    public synchronized void reportMqFailure(String context) {
        mqConsecutiveFailures++;
        log.warn("MQ failure reported [{}] (consecutive={}/{})",
                context, mqConsecutiveFailures, failureThreshold);
        if (mqConsecutiveFailures >= failureThreshold && mqState == State.HEALTHY) {
            mqState = State.DEGRADED;
            mqDegradedAt = System.currentTimeMillis();
            log.error(">>>>> MQ degraded! Consecutive failures={}, threshold={}",
                    mqConsecutiveFailures, failureThreshold);
        }
    }

    public synchronized void reportMqSuccess() {
        if (mqState != State.HEALTHY) {
            log.info("MQ recovered, restoring HEALTHY state");
        }
        mqConsecutiveFailures = 0;
        mqState = State.HEALTHY;
        mqDegradedAt = 0L;
    }

    // ==================== 恢复探针 ====================

    private void checkRecovery(Service service) {
        long degradedAt;
        State currentState;

        if (service == Service.REDIS) {
            degradedAt = redisDegradedAt;
            currentState = redisState;
        } else {
            degradedAt = mqDegradedAt;
            currentState = mqState;
        }

        if (currentState != State.DEGRADED) return;
        if (System.currentTimeMillis() - degradedAt < cooldownMs) return;

        // 冷却期满，执行探针
        boolean recovered = probe(service);
        if (recovered) {
            if (service == Service.REDIS) {
                reportRedisSuccess();
            } else {
                reportMqSuccess();
            }
        } else {
            // 探针失败，重置冷却计时
            if (service == Service.REDIS) {
                redisDegradedAt = System.currentTimeMillis();
            } else {
                mqDegradedAt = System.currentTimeMillis();
            }
        }
    }

    private boolean probe(Service service) {
        try {
            if (service == Service.REDIS) {
                String result = stringRedisTemplate.opsForValue().get("__health_probe__");
                return true; // Redis responded
            } else {
                // MQ 探针：依赖 RabbitTemplate 调用方自行确认
                // 此处只作记录，由下一次成功操作触发 reportMqSuccess()
                log.info("MQ probe: cooldown expired, waiting for next operation to confirm recovery");
                return false;
            }
        } catch (Exception e) {
            log.warn("{} probe failed: {}", service, e.getMessage());
            return false;
        }
    }

    /** 获取当前降级状态（用于监控/日志） */
    public String getStatus() {
        return String.format("Redis: %s (%d failures), MQ: %s (%d failures)",
                redisState, redisConsecutiveFailures,
                mqState, mqConsecutiveFailures);
    }
}
