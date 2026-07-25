package com.spider.media.framework.security;

import com.spider.media.common.constant.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 *
 * <p>继承 OncePerRequestFilter 确保每个请求只执行一次过滤。
 * 职责：从请求头中提取 JWT Token → 解析验证 → 提取用户信息 → 设置到 SecurityContext 中，
 * 使后续的 Controller 和 Service 可以通过 SecurityContextHolder 获取当前登录用户。</p>
 *
 * <p>处理流程：
 * <ol>
 *   <li>从 Authorization 请求头中提取 Token（去除 "Bearer " 前缀）</li>
 *   <li>验证 Token 是否有效（未过期、签名正确）</li>
 *   <li>从 Token 中解析用户名和用户ID</li>
 *   <li>构建 JwtAuthenticationToken 设置到 SecurityContext</li>
 *   <li>无论认证成功与否，都继续执行后续过滤器链</li>
 * </ol></p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT Token 工具类，负责 Token 的解析和验证 */
    private final JwtToken jwtToken;

    public JwtAuthenticationFilter(JwtToken jwtToken) {
        this.jwtToken = jwtToken;
    }

    /**
     * 过滤请求，处理 JWT 认证
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(Constants.HEADER_AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(Constants.TOKEN_PREFIX)) {
            String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
            try {
                if (jwtToken.validateToken(token)) {
                    String username = jwtToken.getUsernameFromToken(token);
                    Long userId = jwtToken.getUserIdFromToken(token);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        JwtAuthenticationToken authToken = new JwtAuthenticationToken(
                                username, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                                userId);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                logger.error("JWT authentication failed", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
