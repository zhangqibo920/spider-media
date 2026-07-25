package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 Mapper 接口
 *
 * <p>定义对 sys_oper_log 表的数据库访问操作，包括日志记录和分页查询。
 * 支持按用户名和模块进行筛选查询。</p>
 */
@Mapper
public interface SysOperLogMapper {

    /**
     * 新增操作日志记录
     *
     * @param log 操作日志实体
     * @return 受影响的行数
     */
    int insert(SysOperLog log);

    /**
     * 分页查询操作日志（支持按用户名和模块筛选）
     *
     * @param username 用户名筛选条件（可为 null 表示不筛选）
     * @param module   模块筛选条件（可为 null 表示不筛选）
     * @param pageNo   起始偏移量（(page-1)*pageSize）
     * @param pageSize 每页条数
     * @return 操作日志列表
     */
    List<SysOperLog> selectPage(@Param("username") String username,
                                 @Param("module") String module,
                                 @Param("pageNo") int pageNo,
                                 @Param("pageSize") int pageSize);

    /**
     * 统计符合条件的操作日志总数（用于分页）
     *
     * @param username 用户名筛选条件（可为 null）
     * @param module   模块筛选条件（可为 null）
     * @return 符合条件的总记录数
     */
    long selectCount(@Param("username") String username,
                     @Param("module") String module);
}
