package com.spider.media.framework.security;

import com.spider.media.common.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 登录用户信息工具类
 *
 * <p>提供获取当前登录用户信息的静态方法，从 Spring Security 上下文中提取用户身份。
 * 如果用户未登录，抛出 ServiceException 异常。</p>
 *
 * <p>使用场景：Controller、Service 层需要获取当前操作用户的 ID 或用户名时调用。</p>
 */
public class LoginUser {

    /**
     * 获取当前登录用户名
     *
     * @return 当前登录用户的用户名
     * @throws ServiceException 未登录时抛出异常
     */
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new ServiceException("未获取到登录用户");
    }

    /**
     * 获取当前登录用户ID
     *
     * <p>从 JwtAuthenticationToken 中提取 userId。需要确保请求携带有效的 JWT Token。</p>
     *
     * @return 当前登录用户的ID
     * @throws ServiceException 未登录或认证信息类型不匹配时抛出异常
     */
    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getUserId();
        }
        throw new ServiceException("未获取到登录用户");
    }
}
