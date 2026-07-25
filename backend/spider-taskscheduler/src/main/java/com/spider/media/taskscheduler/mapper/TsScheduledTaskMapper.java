package com.spider.media.taskscheduler.mapper;

import com.spider.media.taskscheduler.entity.TsScheduledTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 定时任务 Mapper 接口
 *
 * <p>定义对 ts_scheduled_task 表的数据库访问操作，
 * 支持分页查询、新增、按ID查询、更新。</p>
 */
@Mapper
public interface TsScheduledTaskMapper {

    /**
     * 分页查询定时任务（支持按用户ID、任务名、状态筛选）
     *
     * @param userId   用户ID（可为 null 表示不筛选）
     * @param taskName 任务名称模糊查询关键字（可为 null）
     * @param status   任务状态（可为 null）
     * @return 任务列表
     */
    List<TsScheduledTask> selectPage(@Param("userId") Long userId,
                                     @Param("taskName") String taskName,
                                     @Param("status") Integer status);

    /**
     * 新增定时任务
     *
     * @param task 待插入的任务实体
     * @return 受影响的行数
     */
    int insert(TsScheduledTask task);

    /**
     * 根据主键ID查询定时任务
     *
     * @param id 任务ID
     * @return 任务实体，不存在返回 null
     */
    TsScheduledTask selectById(@Param("id") Long id);

    /**
     * 更新定时任务（根据 id 匹配）
     *
     * @param task 待更新的任务实体
     * @return 受影响的行数
     */
    int updateById(TsScheduledTask task);
}
