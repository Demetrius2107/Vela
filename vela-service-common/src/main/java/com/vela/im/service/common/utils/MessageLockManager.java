package com.vela.im.service.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>Title: MessageLockManager</p>
 * <p>Description: 基于 messageKey 的读写锁管理器，协调不同操作（撤回/推送/已读）
 * 对同一条消息的并发访问。写锁→撤回(独占)，读锁→推送/已读(共享)。
 * 锁实例用 WeakReference 语义自动清理。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Component
public class MessageLockManager {

    private static final Logger log = LoggerFactory.getLogger(MessageLockManager.class);

    static final long READ_LOCK_TIMEOUT_MS = 3000L;
    static final long WRITE_LOCK_TIMEOUT_MS = 1000L;

    private final ConcurrentHashMap<Long, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    /** 获取读锁（共享），用于推送、已读等非破坏性操作 */
    public boolean tryReadLock(Long messageKey) {
        if (messageKey == null) return true;
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(messageKey, k -> new ReentrantReadWriteLock());
        try {
            if (lock.readLock().tryLock(READ_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                return true;
            }
            log.warn("ReadLock timeout for messageKey={}, timeout={}ms", messageKey, READ_LOCK_TIMEOUT_MS);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** 释放读锁 */
    public void unlockRead(Long messageKey) {
        if (messageKey == null) return;
        ReentrantReadWriteLock lock = lockMap.get(messageKey);
        if (lock != null && lock.isWriteLockedByCurrentThread()) {
            lock.readLock().unlock();
        } else if (lock != null) {
            lock.readLock().unlock();
        }
    }

    /** 获取写锁（独占），用于撤回等破坏性操作 */
    public boolean tryWriteLock(Long messageKey) {
        if (messageKey == null) return true;
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(messageKey, k -> new ReentrantReadWriteLock());
        try {
            if (lock.writeLock().tryLock(WRITE_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                return true;
            }
            log.warn("WriteLock timeout for messageKey={}, timeout={}ms", messageKey, WRITE_LOCK_TIMEOUT_MS);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** 释放写锁 */
    public void unlockWrite(Long messageKey) {
        if (messageKey == null) return;
        ReentrantReadWriteLock lock = lockMap.get(messageKey);
        if (lock != null && lock.isWriteLockedByCurrentThread()) {
            lock.writeLock().unlock();
        }
    }

    /** 获取当前锁数量（用于监控） */
    public int size() {
        return lockMap.size();
    }
}
