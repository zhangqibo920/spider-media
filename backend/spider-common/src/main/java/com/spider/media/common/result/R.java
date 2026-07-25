package com.spider.media.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果包装类
 *
 * <p>所有 API 接口的统一响应格式，参考 RuoYi 的 CommonResult 设计。
 * 约定：code == 0 表示操作成功，code != 0 表示操作失败，同时携带提示信息和数据。</p>
 *
 * @param <T> 响应数据的泛型类型
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 错误码，0 表示成功，非 0 表示失败（参见 {@link ErrorCodeEnums}） */
    private int code;

    /** 提示信息，成功时为"操作成功"，失败时为具体错误描述 */
    private String message;

    /** 响应数据，成功时返回业务数据，失败时为 null */
    private T data;

    // ========== 成功响应 ==========

    /** 成功（无数据） */
    public static <T> R<T> ok() {
        return restResult(null, 0, "操作成功");
    }

    /** 成功（携带数据） */
    public static <T> R<T> ok(T data) {
        return restResult(data, 0, "操作成功");
    }

    /** 成功（携带数据和自定义提示信息） */
    public static <T> R<T> ok(T data, String message) {
        return restResult(data, 0, message);
    }

    // ========== 失败响应 ==========

    /** 失败（使用默认系统错误码） */
    public static <T> R<T> fail() {
        return fail(ErrorCodeEnums.SYSTEM_ERROR);
    }

    /** 失败（自定义错误信息） */
    public static <T> R<T> fail(String message) {
        return restResult(null, ErrorCodeEnums.SYSTEM_ERROR.getCode(), message);
    }

    /** 失败（自定义错误码和错误信息） */
    public static <T> R<T> fail(int code, String message) {
        return restResult(null, code, message);
    }

    /** 失败（使用错误码枚举） */
    public static <T> R<T> fail(ErrorCodeEnums errorCode) {
        return restResult(null, errorCode.getCode(), errorCode.getMessage());
    }

    /** 失败（使用错误码枚举，覆盖默认错误信息） */
    public static <T> R<T> fail(ErrorCodeEnums errorCode, String message) {
        return restResult(null, errorCode.getCode(), message);
    }

    // ========== 内部构建方法 ==========

    /** 构建统一响应结果对象 */
    private static <T> R<T> restResult(T data, int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMessage(message);
        return r;
    }

    /** 判断当前响应是否成功（code == 0） */
    public boolean isSuccess() {
        return this.code == 0;
    }
}
