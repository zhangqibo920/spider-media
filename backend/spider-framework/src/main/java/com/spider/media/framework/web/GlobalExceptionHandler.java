package com.spider.media.framework.web;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.common.result.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>通过 @RestControllerAdvice 统一捕获和处理所有 Controller 层抛出的异常，
 * 将异常转换为标准的 JSON 错误响应格式，避免将堆栈信息暴露给前端。</p>
 *
 * <p>处理的异常类型：
 * <ul>
 *   <li>ServiceException - 业务异常，返回对应的错误码和错误信息</li>
 *   <li>MethodArgumentNotValidException - @RequestBody 参数校验失败</li>
 *   <li>BindException - 表单参数绑定失败</li>
 *   <li>BadCredentialsException - 用户名或密码错误</li>
 *   <li>AccessDeniedException - 权限不足</li>
 *   <li>HttpRequestMethodNotSupportedException - 请求方法不支持</li>
 *   <li>Exception - 兜底处理所有未预料的系统异常</li>
 * </ul></p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常（ServiceException）
     *
     * @param e 业务异常
     * @return 包含错误码和错误信息的统一响应
     */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e) {
        log.error("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 @RequestBody 参数校验异常
     *
     * @param e 参数校验异常
     * @return 包含第一个校验错误信息的统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return R.fail(ErrorCodeEnums.PARAM_ERROR, message);
    }

    /**
     * 处理表单参数绑定异常
     *
     * @param e 参数绑定异常
     * @return 包含第一个错误信息的统一响应
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return R.fail(ErrorCodeEnums.PARAM_ERROR, message);
    }

    /**
     * 处理认证异常（用户名或密码错误）
     *
     * @param e 认证异常
     * @return 密码错误的统一响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    public R<Void> handleBadCredentialsException(BadCredentialsException e) {
        return R.fail(ErrorCodeEnums.SYS_PASSWORD_ERROR);
    }

    /**
     * 处理权限不足异常
     *
     * @param e 权限异常
     * @return 权限不足的统一响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDeniedException(AccessDeniedException e) {
        return R.fail(ErrorCodeEnums.FORBIDDEN);
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e 请求方法不支持异常
     * @return 包含不支持的 HTTP 方法名的统一响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return R.fail(ErrorCodeEnums.METHOD_NOT_ALLOWED, "不支持'" + e.getMethod() + "'请求");
    }

    /**
     * 兜底处理所有未预料的系统异常
     *
     * @param e 系统异常
     * @return 系统错误的统一响应（不暴露异常详情给前端）
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(ErrorCodeEnums.SYSTEM_ERROR);
    }
}
