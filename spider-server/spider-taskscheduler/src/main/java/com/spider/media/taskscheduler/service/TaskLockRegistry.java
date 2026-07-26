package com.spider.media.taskscheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 任务并发控制组件
 *
 * <p>基于 Redis 实现分布式任务锁，防止定时任务在多实例环境下重复执行，
 * 同时防止同一任务在前一次执行未完成时被再次触发（防重入）。</p>
 *
 * <p>锁策略：
 * <ul>
 *   <li>使用 SETNX + 过期时间实现互斥锁</li>
 *   <li>锁 Key 格式：task:lock:{taskId}</li>
 *   <li>默认锁超时 30 分钟，防止任务异常退出导致死锁</li>
 *   <li>获取锁失败时返回 false，调用方应跳过本次执行</li>
 * </ul></p>
 *
 * <p>使用示例：
 * <pre>
 * if (taskLockRegistry.tryLock(taskId)) {
 *     try {
 *         // 执行任务逻辑
 *     } finally {
 *         taskLockRegistry.unlock(taskId);
 *     }
 * }
 * </pre></p>
 */
@Component
public class TaskLockRegistry {

    private static final Logger log = LoggerFactory.getLogger(TaskLockRegistry.class);

    /** Redis Key 前缀：任务锁 */
    private static final String TASK_LOCK_KEY_PREFIX = "task:lock:";

    /** 默认锁超时时间（分钟）—— 防止任务异常退出导致死锁 */
    private static final long DEFAULT_LOCK_TIMEOUT_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TaskLockRegistry(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取任务锁
     *
     * <p>使用 Redis SETNX 原子操作实现互斥锁。若锁已被占用（任务正在执行），
     * 立即返回 false，避免任务重入。</p>
     *
     * @param taskId 任务ID
     * @return true=获取锁成功；false=锁已被占用（任务正在执行）
     */
    public boolean tryLock(Long taskId) {
        return tryLock(taskId, DEFAULT_LOCK_TIMEOUT_MINUTES);
    }

    /**
     * 尝试获取任务锁（自定义超时时间）
     *
     * @param taskId        任务ID
     * @param timeoutMinutes 锁超时时间（分钟）
     * @return true=获取锁成功；false=锁已被占用
     */
    public boolean tryLock(Long taskId, long timeoutMinutes) {
        if (taskId == null) {
            return false;
        }
        String key = TASK_LOCK_KEY_PREFIX + taskId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                key,
                String.valueOf(System.currentTimeMillis()),
                timeoutMinutes,
                TimeUnit.MINUTES
        );
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("任务锁获取成功, taskId={}", taskId);
            return true;
        }
        log.warn("任务锁获取失败（任务正在执行），taskId={}", taskId);
        return false;
    }

    /**
     * 释放任务锁
     *
     * <p>任务执行完成后必须调用此方法释放锁，否则锁会一直占用直到超时。
     * 使用 try-finally 确保异常情况下也能释放锁。</p>
     *
     * @param taskId 任务ID
     */
    public void unlock(Long taskId) {
        if (taskId == null) {
            return;
        }
        Boolean deleted = redisTemplate.delete(TASK_LOCK_KEY_PREFIX + taskId);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("任务锁释放成功, taskId={}", taskId);
        }
    }

    /**
     * 检查任务是否正在执行（锁是否被占用）
     *
     * @param taskId 任务ID
     * @return true=任务正在执行；false=任务未执行
     */
    public boolean isRunning(Long taskId) {
        if (taskId == null) {
            return false;
        }
        Boolean hasKey = redisTemplate.hasKey(TASK_LOCK_KEY_PREFIX + taskId);
        return Boolean.TRUE.equals(hasKey);
    }
}
