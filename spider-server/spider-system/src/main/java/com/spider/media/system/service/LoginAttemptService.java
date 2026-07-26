package com.spider.media.system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败计数服务
 *
 * <p>基于 Redis 实现登录失败次数追踪和账号临时锁定。
 * 防止暴力破解密码攻击。</p>
 *
 * <p>策略：
 * <ul>
 *   <li>每次登录失败后，失败计数 +1，并刷新 TTL（默认 10 分钟滑动窗口）</li>
 *   <li>当失败次数达到阈值（默认 5 次）时，设置锁定标记，TTL = 锁定时长（默认 5 分钟）</li>
 *   <li>登录成功后清空失败计数</li>
 *   <li>所有计数和锁定均使用 Redis TTL 自动过期，无需手动清理</li>
 * </ul></p>
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    /** Redis Key 前缀：登录失败计数 */
    private static final String FAIL_COUNT_KEY_PREFIX = "login_fail:";

    /** Redis Key 前缀：账号锁定标记 */
    private static final String LOCK_KEY_PREFIX = "login_lock:";

    /** 最大失败次数（达到后锁定账号） */
    private static final int MAX_ATTEMPTS = 5;

    /** 失败计数窗口（分钟）—— 滑动窗口，超过此时间自动清零 */
    private static final long FAIL_WINDOW_MINUTES = 10;

    /** 锁定时长（分钟） */
    private static final long LOCK_DURATION_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public LoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查账号是否已被锁定
     *
     * @param username 用户名
     * @return true=已锁定，需拒绝登录；false=未锁定，可继续验证
     */
    public boolean isLocked(String username) {
        Boolean hasKey = redisTemplate.hasKey(LOCK_KEY_PREFIX + username);
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * 获取账号剩余锁定时间（秒）
     *
     * @param username 用户名
     * @return 剩余锁定秒数，未锁定返回 0
     */
    public long getRemainingLockTime(String username) {
        Long ttl = redisTemplate.getExpire(LOCK_KEY_PREFIX + username, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 记录一次登录失败
     *
     * <p>失败计数 +1。若达到阈值，则设置锁定标记。
     * 计数器使用滑动窗口策略，每次失败都会刷新 TTL。</p>
     *
     * @param username 用户名
     */
    public void recordFailure(String username) {
        String failKey = FAIL_COUNT_KEY_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null) {
            // 第一次失败时设置 TTL（滑动窗口）
            if (count == 1) {
                redisTemplate.expire(failKey, FAIL_WINDOW_MINUTES, TimeUnit.MINUTES);
            }
            // 达到阈值，设置锁定标记
            if (count >= MAX_ATTEMPTS) {
                redisTemplate.opsForValue().set(
                        LOCK_KEY_PREFIX + username,
                        "1",
                        LOCK_DURATION_MINUTES,
                        TimeUnit.MINUTES
                );
                log.warn("账号 {} 因连续登录失败 {} 次被锁定 {} 分钟",
                        username, count, LOCK_DURATION_MINUTES);
            }
        }
    }

    /**
     * 登录成功后清空失败计数
     *
     * @param username 用户名
     */
    public void recordSuccess(String username) {
        redisTemplate.delete(FAIL_COUNT_KEY_PREFIX + username);
        redisTemplate.delete(LOCK_KEY_PREFIX + username);
    }

    /**
     * 获取当前失败次数（用于前端提示剩余可尝试次数）
     *
     * @param username 用户名
     * @return 失败次数，无记录返回 0
     */
    public int getFailCount(String username) {
        String count = redisTemplate.opsForValue().get(FAIL_COUNT_KEY_PREFIX + username);
        if (count == null) {
            return 0;
        }
        try {
            return Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }
}
