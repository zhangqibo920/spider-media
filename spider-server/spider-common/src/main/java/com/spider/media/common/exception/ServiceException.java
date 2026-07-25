package com.spider.media.common.exception;

import com.spider.media.common.result.ErrorCodeEnums;

/**
 * 自定义业务异常
 *
 * <p>用于在业务逻辑层抛出带有错误码和错误信息的异常，由 {@link com.spider.media.framework.web.GlobalExceptionHandler}
 * 统一捕获并转换为标准的错误响应格式。</p>
 *
 * <p>使用场景：参数校验不通过、业务规则不满足、数据不存在等需要返回明确错误信息的情况。</p>
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码，用于前端识别和后端日志追踪 */
    private final int code;

    /**
     * 使用默认系统错误码（1）和自定义错误信息
     *
     * @param message 错误信息
     */
    public ServiceException(String message) {
        super(message);
        this.code = ErrorCodeEnums.SYSTEM_ERROR.getCode();
    }

    /**
     * 使用自定义错误码和错误信息
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用错误码枚举（错误信息从枚举中获取）
     *
     * @param errorCode 错误码枚举
     */
    public ServiceException(ErrorCodeEnums errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码枚举，覆盖默认错误信息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息（覆盖枚举中的默认信息）
     */
    public ServiceException(ErrorCodeEnums errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 获取业务错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}
