package com.spider.media.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全相关工具类
 *
 * <p>提供密码加密、密码验证、获取当前登录用户等安全相关的便捷方法。
 * 使用 BCrypt 算法进行密码的单向加密存储，确保即使数据库泄露也不会暴露明文密码。</p>
 */
public class SecurityUtils {

    /** BCrypt 密码编码器（线程安全，可复用） */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 获取当前登录用户名
     *
     * <p>从 Spring Security 上下文中提取已认证用户的用户名。
     * 如果未登录或认证信息不存在，返回默认值 "system"。</p>
     *
     * @return 当前登录用户名，未登录时返回 "system"
     */
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }

    /**
     * 使用 BCrypt 算法加密明文密码
     *
     * @param password 明文密码
     * @return 加密后的 BCrypt 哈希值
     */
    public static String encryptPassword(String password) {
        return ENCODER.encode(password);
    }

    /**
     * 验证明文密码与加密密码是否匹配
     *
     * @param rawPassword     用户输入的明文密码
     * @param encodedPassword 数据库中存储的 BCrypt 哈希值
     * @return 匹配返回 true，不匹配返回 false
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
