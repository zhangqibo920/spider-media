package com.spider.media.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果（参考 RuoYi CommonResult）
 *
 * 约定：code == 0 表示成功，非 0 表示失败
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 错误码，0 表示成功，非 0 表示失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 数据 */
    private T data;

    // ========== 成功 ==========

    public static <T> R<T> ok() {
        return restResult(null, 0, "操作成功");
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, 0, "操作成功");
    }

    public static <T> R<T> ok(T data, String message) {
        return restResult(data, 0, message);
    }

    // ========== 失败 ==========

    public static <T> R<T> fail() {
        return fail(ErrorCodeEnums.SYSTEM_ERROR);
    }

    public static <T> R<T> fail(String message) {
        return restResult(null, ErrorCodeEnums.SYSTEM_ERROR.getCode(), message);
    }

    public static <T> R<T> fail(int code, String message) {
        return restResult(null, code, message);
    }

    public static <T> R<T> fail(ErrorCodeEnums errorCode) {
        return restResult(null, errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> R<T> fail(ErrorCodeEnums errorCode, String message) {
        return restResult(null, errorCode.getCode(), message);
    }

    // ========== 内部方法 ==========

    private static <T> R<T> restResult(T data, int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMessage(message);
        return r;
    }

    public boolean isSuccess() {
        return this.code == 0;
    }
}
