package com.spider.media.framework.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JWT 认证 Token 对象
 *
 * <p>扩展 Spring Security 的 UsernamePasswordAuthenticationToken，在标准认证信息之外
 * 额外携带 userId 和 role 字段，方便在业务层获取当前登录用户的身份（如数据权限过滤、角色判断）。</p>
 *
 * <p>在 JwtAuthenticationFilter 中构建并设置到 SecurityContext，
 * 后续可通过 {@link LoginUser#getUserId()} / {@link LoginUser#getRole()} 获取。</p>
 */
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    /** 当前登录用户的 ID */
    private final Long userId;

    /** 当前登录用户的角色（USER / ADMIN） */
    private final String role;

    /**
     * 构造 JWT 认证 Token
     *
     * @param principal   主体信息（通常是用户名）
     * @param credentials 凭证信息（JWT 场景下通常为 null）
     * @param authorities 用户权限列表
     * @param userId      用户ID
     * @param role        用户角色（USER / ADMIN）
     */
    public JwtAuthenticationToken(Object principal, Object credentials,
                                   Collection<? extends GrantedAuthority> authorities,
                                   Long userId, String role) {
        super(principal, credentials, authorities);
        this.userId = userId;
        this.role = role;
    }

    /**
     * 获取当前登录用户的 ID
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取当前登录用户的角色
     *
     * @return 用户角色（USER / ADMIN）
     */
    public String getRole() {
        return role;
    }
}
