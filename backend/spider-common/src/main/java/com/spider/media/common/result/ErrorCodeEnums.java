package com.spider.media.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 *
 * 错误码设计参考 RuoYi：
 * - 0：成功
 * - 1-999：系统错误码（对应 HTTP 状态码）
 * - 1000-999999：业务错误码，按模块分段
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnums {

    // ========== 系统级错误码 0-999 ==========

    SUCCESS(0, "操作成功"),
    SYSTEM_ERROR(1, "系统执行出错"),
    PARAM_ERROR(2, "参数不正确"),
    NOT_FOUND(4, "请求资源不存在"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "没有权限"),
    METHOD_NOT_ALLOWED(405, "请求方法不正确"),
    TOO_MANY_REQUESTS(429, "请求太过频繁，请稍后再试"),
    INTERNAL_SERVER_ERROR(500, "系统执行出错"),

    // ========== 业务错误码 1000-999999（按模块分段） ==========
    // 每个模块分配 100000 的区间：1xxxxx, 2xxxxx, ...

    // ----- 数据采集模块 100000-199999 -----
    DC_TARGET_ACCOUNT_NOT_FOUND(100001, "对标账号不存在"),
    DC_ARTICLE_NOT_FOUND(100002, "采集文章不存在"),
    DC_COLLECT_TASK_FAILED(100003, "采集任务执行失败"),

    // ----- AI 创作模块 200000-299999 -----
    AC_HOT_TOPIC_NOT_FOUND(200001, "热点话题不存在"),
    AC_ARTICLE_GENERATE_FAILED(200002, "文章生成失败"),
    AC_MODEL_NOT_AVAILABLE(200003, "AI 模型不可用"),

    // ----- 内容发布模块 300000-399999 -----
    PB_ACCOUNT_NOT_FOUND(300001, "发布账号不存在"),
    PB_TASK_NOT_FOUND(300002, "发布任务不存在"),
    PB_PUBLISH_FAILED(300003, "发布失败"),

    // ----- 任务调度模块 400000-499999 -----
    TS_TASK_NOT_FOUND(400001, "定时任务不存在"),
    TS_TASK_ENABLE_FAILED(400002, "任务启用失败"),
    TS_TASK_DISABLE_FAILED(400003, "任务停用失败"),

    // ----- 系统模块 900000-999999 -----
    SYS_USER_NOT_FOUND(900001, "用户不存在"),
    SYS_USERNAME_ALREADY_EXISTS(900002, "用户名已存在"),
    SYS_PASSWORD_ERROR(900003, "密码错误"),
    ;

    /** 错误码 */
    private final int code;

    /** 错误信息 */
    private final String message;
}
