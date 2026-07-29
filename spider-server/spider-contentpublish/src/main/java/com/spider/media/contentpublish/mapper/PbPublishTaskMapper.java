package com.spider.media.contentpublish.mapper;

import com.spider.media.contentpublish.entity.PbPublishTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发布任务 Mapper 接口
 *
 * <p>定义对 pb_publish_task 表的数据库访问操作，
 * 支持分页查询、新增、按ID查询、更新任务状态。</p>
 */
@Mapper
public interface PbPublishTaskMapper {

    /**
     * 分页查询发布任务（支持按用户ID、平台、状态筛选）
     *
     * @param userId   用户ID（可为 null 表示不筛选）
     * @param platform 平台类型（可为 null）
     * @param status   任务状态（可为 null）
     * @return 任务列表
     */
    List<PbPublishTask> selectPage(@Param("userId") Long userId,
                                   @Param("platform") String platform,
                                   @Param("status") Integer status);

    /**
     * 新增发布任务
     *
     * @param task 待插入的任务实体
     * @return 受影响的行数
     */
    int insert(PbPublishTask task);

    /**
     * 根据主键ID查询发布任务
     *
     * @param id 任务ID
     * @return 任务实体，不存在返回 null
     */
    PbPublishTask selectById(@Param("id") Long id);

    /**
     * 更新发布任务（根据 id 匹配，用于更新状态和发布结果）
     *
     * @param task 待更新的任务实体
     * @return 受影响的行数
     */
    int updateById(PbPublishTask task);

    /**
     * 逻辑删除发布任务
     *
     * @param id 任务ID
     * @return 受影响的行数
     */
    int deleteById(@Param("id") Long id);
}
