package com.spider.media.aicreation.mapper;

import com.spider.media.aicreation.entity.AcHotTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 热点话题 Mapper 接口
 *
 * <p>定义对 ac_hot_topic 表的数据库访问操作，
 * 支持按 ID 查询、按用户查询、新增、按用户和平台批量删除。</p>
 */
@Mapper
public interface AcHotTopicMapper {

    /**
     * 根据主键ID查询热点话题
     *
     * @param id 话题ID
     * @return 话题实体，不存在返回 null
     */
    AcHotTopic selectById(@Param("id") Long id);

    /**
     * 查询指定用户的所有热点话题
     *
     * @param userId 用户ID
     * @return 该用户的所有热点话题列表
     */
    List<AcHotTopic> selectByUserId(@Param("userId") Long userId);

    /**
     * 新增热点话题
     *
     * @param topic 待插入的话题实体
     * @return 受影响的行数
     */
    int insert(AcHotTopic topic);

    /**
     * 按用户ID和平台删除热点话题（用于刷新热点前清空旧数据）
     *
     * @param userId   用户ID
     * @param platform 平台类型
     * @return 受影响的行数
     */
    int deleteByUserId(@Param("userId") Long userId, @Param("platform") String platform);
}
