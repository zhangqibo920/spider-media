package com.spider.media.aicreation.mapper;

import com.spider.media.aicreation.entity.AiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 模型 Mapper 接口
 */
@Mapper
public interface AiModelMapper {

    /** 查询所有模型（按排序号升序） */
    List<AiModel> selectAll();

    /** 根据模型标识查询 */
    AiModel selectByModelKey(@Param("modelKey") String modelKey);

    /** 根据ID查询 */
    AiModel selectById(@Param("id") Long id);

    /** 新增模型 */
    int insert(AiModel model);

    /** 更新模型 */
    int update(AiModel model);

    /** 更新测试状态 */
    int updateTestStatus(@Param("id") Long id,
                         @Param("testStatus") String testStatus,
                         @Param("testMessage") String testMessage);

    /** 根据ID删除模型 */
    int deleteById(@Param("id") Long id);
}
