package com.spider.media.system.service;

import com.spider.media.system.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志业务层接口
 *
 * <p>定义操作日志的记录和查询操作。
 * 由 {@link com.spider.media.system.service.impl.SysOperLogServiceImpl} 提供具体实现。</p>
 */
public interface ISysOperLogService {

    /**
     * 记录一条操作日志
     *
     * @param log 操作日志实体
     */
    void recordLog(SysOperLog log);

    /**
     * 分页查询操作日志
     *
     * @param username 用户名筛选（可为 null）
     * @param module   模块筛选（可为 null）
     * @param pageNo   起始偏移量（(page-1)*pageSize）
     * @param pageSize 每页条数
     * @return 操作日志列表
     */
    List<SysOperLog> selectLogPage(String username, String module, int pageNo, int pageSize);

    /**
     * 统计符合条件的操作日志总数
     *
     * @param username 用户名筛选（可为 null）
     * @param module   模块筛选（可为 null）
     * @return 总记录数
     */
    long selectLogCount(String username, String module);
}
