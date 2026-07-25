package com.spider.media.common.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Servlet 工具类
 *
 * <p>提供从 Spring Request 上下文中获取 HttpServletRequest、请求头、请求参数等便捷方法。
 * 适用于非 Controller 层（如 Service、Filter）中需要获取当前请求信息的场景。</p>
 */
public class ServletUtils {

    /**
     * 获取当前线程绑定的 HttpServletRequest 对象
     *
     * @return 当前请求的 HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取当前线程绑定的 ServletRequestAttributes
     *
     * @return 当前请求的 ServletRequestAttributes
     */
    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 获取指定名称的请求头值
     *
     * @param name 请求头名称
     * @return 请求头值
     */
    public static String getHeader(String name) {
        return getRequest().getHeader(name);
    }

    /**
     * 获取指定名称的请求参数值
     *
     * @param name 参数名称
     * @return 参数值，不存在时返回 null
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取指定名称的请求参数值（带默认值）
     *
     * @param name         参数名称
     * @param defaultValue 参数不存在时的默认值
     * @return 参数值，不存在时返回默认值
     */
    public static String getParameter(String name, String defaultValue) {
        String value = getParameter(name);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取指定名称的请求参数并转换为 Integer 类型
     *
     * @param name         参数名称
     * @param defaultValue 参数不存在或转换失败时的默认值
     * @return 参数的整型值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        String value = getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
}
