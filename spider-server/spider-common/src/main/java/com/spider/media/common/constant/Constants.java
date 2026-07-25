package com.spider.media.common.constant;

/**
 * 全局通用常量类
 *
 * <p>集中管理系统中使用的常量值，包括字符集、HTTP 状态码、认证相关常量、逻辑删除标志等。
 * 避免在代码中硬编码魔法值，提高可维护性和一致性。</p>
 */
public class Constants {

    /** UTF-8 字符集编码 */
    public static final String UTF8 = "UTF-8";

    /** 通用成功标识（HTTP 200） */
    public static final int SUCCESS = 200;

    /** 通用失败标识（HTTP 500） */
    public static final int FAIL = 500;

    /** 登录成功状态标识 */
    public static final String LOGIN_SUCCESS = "Success";

    /** 登录失败状态标识 */
    public static final String LOGIN_FAIL = "Error";

    /** HTTP Authorization 请求头中 Token 的前缀，格式为 "Bearer " */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** HTTP Authorization 请求头名称 */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /** HTTP 401 未授权状态码 */
    public static final int UNAUTHORIZED = 401;

    /** HTTP 403 禁止访问状态码 */
    public static final int FORBIDDEN = 403;

    /** 用户名字段常量，用于从请求中提取用户身份信息 */
    public static final String USERNAME = "username";

    /** 逻辑删除标志：已删除 */
    public static final String DELETED = "2";

    /** 逻辑删除标志：未删除（正常状态） */
    public static final String NOT_DELETED = "0";
}
