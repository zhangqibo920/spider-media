package com.spider.media.framework.security;

import com.spider.media.common.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 登录用户信息工具类
 */
public class LoginUser {

    /**
     * 获取当前登录用户名
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
     */
    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getUserId();
        }
        throw new ServiceException("未获取到登录用户");
    }
}
