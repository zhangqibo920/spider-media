package com.spider.media.system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 *
 * <p>对应数据库表 sys_oper_log，记录用户在系统中的操作行为，用于审计追踪和问题排查。
 * 记录操作者、操作模块、操作类型、请求参数、执行结果等完整信息。</p>
 */
@Data
public class SysOperLog implements Serializable {

    /** 日志主键ID */
    private Long id;

    /** 操作者的用户名 */
    private String username;

    /** 操作模块（如 "用户管理"、"配置管理"） */
    private String module;

    /** 操作类型（如 "新增"、"修改"、"删除"、"查询"） */
    private String action;

    /** 操作的详细描述信息 */
    private String description;

    /** 操作者的 IP 地址 */
    private String ip;

    /** 请求的 HTTP 方法（GET/POST/PUT/DELETE） */
    private String method;

    /** 请求参数的 JSON 字符串（用于问题复现和审计） */
    private String params;

    /** 操作结果状态：0=成功，其他值表示失败 */
    private Integer status;

    /** 如果操作失败，记录错误信息 */
    private String errorMsg;

    /** 操作执行的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
