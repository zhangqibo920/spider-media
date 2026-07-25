package com.spider.media.system.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.media.system.entity.SysOperLog;
import com.spider.media.system.service.ISysOperLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 *
 * <p>通过 AOP 自动拦截标注了 @OperLog 注解的 Controller 方法，
 * 在方法执行前后记录操作日志，包括操作者、模块、方法、参数、IP、执行结果等信息。</p>
 *
 * <p>使用方式：在 Controller 方法上添加 @OperLog 注解即可自动记录日志。
 * 日志记录失败不会影响正常业务流程。</p>
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 操作日志业务层服务 */
    private final ISysOperLogService operLogService;
    /** JSON 序列化工具（用于序列化请求参数） */
    private final ObjectMapper objectMapper;

    public LogAspect(ISysOperLogService operLogService, ObjectMapper objectMapper) {
        this.operLogService = operLogService;
        this.objectMapper = objectMapper;
    }

    /** 切入点：所有标注了 @OperLog 注解的方法 */
    @Pointcut("@annotation(com.spider.media.system.aspect.OperLog)")
    public void logPointcut() {
    }

    /**
     * 环绕通知：在方法执行前后记录操作日志
     *
     * @param joinPoint 连接点
     * @param operLog   操作日志注解
     * @return 方法执行结果
     */
    @Around("logPointcut() && @annotation(operLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        SysOperLog sysOperLog = new SysOperLog();
        sysOperLog.setModule(operLog.module());
        sysOperLog.setAction(operLog.action());
        sysOperLog.setCreateTime(LocalDateTime.now());

        // 获取当前请求信息（IP、HTTP方法）
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysOperLog.setIp(getClientIp(request));
                sysOperLog.setMethod(request.getMethod());
            }
        } catch (Exception e) {
            log.debug("获取请求信息失败", e);
        }

        // 获取当前登录用户名
        try {
            sysOperLog.setUsername(com.spider.media.framework.security.LoginUser.getUsername());
        } catch (Exception e) {
            sysOperLog.setUsername("anonymous");
        }

        // 序列化请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                sysOperLog.setParams(objectMapper.writeValueAsString(args));
            }
        } catch (Exception e) {
            log.debug("序列化请求参数失败", e);
        }

        // 执行目标方法并记录结果
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            sysOperLog.setStatus(0);
            sysOperLog.setDescription(operLog.action() + "成功");
        } catch (Throwable e) {
            sysOperLog.setStatus(1);
            sysOperLog.setErrorMsg(e.getMessage());
            sysOperLog.setDescription(operLog.action() + "失败: " + e.getMessage());
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.debug("操作耗时: {}ms, 模块: {}, 操作: {}", costTime, operLog.module(), operLog.action());
            operLogService.recordLog(sysOperLog);
        }

        return result;
    }

    /**
     * 获取客户端真实 IP 地址（支持代理转发场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
