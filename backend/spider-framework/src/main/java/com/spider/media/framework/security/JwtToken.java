package com.spider.media.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 *
 * <p>负责 JWT Token 的创建、解析、验证和信息提取。
 * 使用 HMAC-SHA 算法进行签名，密钥和过期时间从 application.yml 中读取。</p>
 *
 * <p>Token 中包含以下信息：
 * <ul>
 *   <li>subject: 用户名</li>
 *   <li>userId: 用户ID（自定义 Claim）</li>
 *   <li>issuedAt: 签发时间</li>
 *   <li>expiration: 过期时间</li>
 * </ul></p>
 */
@Component
public class JwtToken {

    /** Token 中存储用户ID的 Claim 名称 */
    private static final String CLAIM_USER_ID = "userId";

    /** JWT 签名密钥（从配置文件读取） */
    @Value("${token.secret}")
    private String secret;

    /** Token 过期时间（毫秒，从配置文件读取） */
    @Value("${token.expireTime}")
    private long expireTime;

    /** 密钥对象（使用 volatile + 双重检查锁定保证线程安全的懒加载） */
    private volatile SecretKey secretKey;

    /**
     * 获取签名密钥（双重检查锁定的懒加载单例）
     *
     * @return HMAC-SHA 签名密钥
     */
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            synchronized (this) {
                if (secretKey == null) {
                    secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        return secretKey;
    }

    /**
     * 创建 JWT Token
     *
     * @param username 用户名（作为 Token 的 subject）
     * @param userId   用户ID（存储在自定义 Claim 中）
     * @return 签名后的 JWT Token 字符串
     */
    public String createToken(String username, Long userId) {
        SecretKey key = getSecretKey();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_USER_ID, userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token 字符串
     * @return 用户名，解析失败返回 null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT Token 字符串
     * @return 用户ID，解析失败返回 null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get(CLAIM_USER_ID);
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        return null;
    }

    /**
     * 验证 Token 是否有效（签名正确且未过期）
     *
     * @param token JWT Token 字符串
     * @return 有效返回 true，无效返回 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 JWT Token 并返回 Claims
     *
     * @param token JWT Token 字符串
     * @return 解析后的 Claims 对象
     * @throws Exception Token 无效时抛出异常
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
