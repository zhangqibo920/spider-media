package com.spider.media.system.aspect;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * <p>标注在 Controller 方法上，由 {@link LogAspect} 拦截并自动记录操作日志。
 * 包含模块名和操作类型两个属性，用于日志分类和查询筛选。</p>
 *
 * <p>使用示例：
 * <pre>
 *   {@literal @}OperLog(module = "用户管理", action = "新增")
 *   {@literal @}PostMapping("/user")
 *   public R&lt;Void&gt; addUser(@RequestBody SysUser user) { ... }
 * </pre></p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 操作模块名称（如 "用户管理"、"配置管理"、"发布管理"） */
    String module() default "";

    /** 操作类型（如 "新增"、"修改"、"删除"、"查询"、"登录"、"注册"） */
    String action() default "";
}
