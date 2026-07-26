package com.spider.media.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 *
 * <p>负责 JWT Token 的创建、解析、验证和信息提取。
 * 使用 HMAC-SHA256 算法进行签名，密钥和过期时间从 application.yml 中读取。</p>
 *
 * <p>Token 中包含以下信息：
 * <ul>
 *   <li>subject: 用户名</li>
 *   <li>userId: 用户ID（自定义 Claim）</li>
 *   <li>role: 用户角色（自定义 Claim，USER / ADMIN）</li>
 *   <li>issuedAt: 签发时间</li>
 *   <li>expiration: 过期时间</li>
 * </ul></p>
 *
 * <p>安全说明：
 * <ul>
 *   <li>密钥从配置项 {@code token.secret} 读取，必须显式配置（无默认值），否则启动失败 —— 避免使用弱密钥</li>
 *   <li>密钥长度需满足 HMAC-SHA256 的最低要求（≥ 32 字节）</li>
 *   <li>签名算法明确使用 HS256，避免算法降级攻击</li>
 * </ul></p>
 */
@Component
public class JwtToken {

    private static final Logger log = LoggerFactory.getLogger(JwtToken.class);

    /** Token 中存储用户ID的 Claim 名称 */
    private static final String CLAIM_USER_ID = "userId";

    /** Token 中存储用户角色的 Claim 名称 */
    private static final String CLAIM_ROLE = "role";

    /** 默认角色（当 Token 中缺少 role claim 时使用，遵循最小权限原则） */
    private static final String DEFAULT_ROLE = "USER";

    /** JWT 签名密钥（从配置文件读取，必须显式配置） */
    @Value("${token.secret}")
    private String secret;

    /** Token 过期时间（毫秒，从配置文件读取） */
    @Value("${token.expireTime}")
    private long expireTime;

    /** 密钥对象（启动时初始化，使用 volatile 保证可见性） */
    private volatile SecretKey secretKey;

    /**
     * 启动时校验并初始化签名密钥
     *
     * <p>在 Bean 初始化阶段校验密钥配置，确保：
     * <ul>
     *   <li>secret 非空（未配置时启动失败，避免使用弱密钥或空密钥）</li>
     *   <li>secret 满足 HS256 算法的最低长度要求（≥ 32 字节）</li>
     * </ul></p>
     *
     * @throws WeakKeyException 密钥长度不足时抛出
     * @throws IllegalStateException 密钥未配置时抛出
     */
    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "配置项 token.secret 未设置，请在环境变量或配置文件中提供至少 32 字符的 JWT 密钥");
        }
        // Keys.hmacShaKeyFor 内部会校验密钥长度，不满足 HS256 要求时抛出 WeakKeyException
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT 签名密钥已初始化，算法=HS256");
    }

    /**
     * 创建 JWT Token
     *
     * @param username 用户名（作为 Token 的 subject）
     * @param userId   用户ID（存储在自定义 Claim 中）
     * @param role     用户角色（USER / ADMIN，存储在自定义 Claim 中）
     * @return 签名后的 JWT Token 字符串
     */
    public String createToken(String username, Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role == null ? DEFAULT_ROLE : role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
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
     * 从 Token 中提取用户角色
     *
     * @param token JWT Token 字符串
     * @return 用户角色（USER / ADMIN），解析失败或缺失时返回 {@link #DEFAULT_ROLE}
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object role = claims.get(CLAIM_ROLE);
            if (role instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return DEFAULT_ROLE;
    }

    /**
     * 验证 Token 是否有效（签名正确且未过期）
     *
     * @param token JWT Token 字符串
     * @return 有效返回 true，无效或解析失败返回 false
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 解析 JWT Token 并返回 Claims
     *
     * @param token JWT Token 字符串
     * @return 解析后的 Claims 对象；解析失败返回 null（避免上层因异常流程陷入 NPE）
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("JWT Token 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
