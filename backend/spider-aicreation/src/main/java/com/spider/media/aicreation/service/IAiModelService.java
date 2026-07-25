package com.spider.media.aicreation.service;

import com.spider.media.aicreation.entity.AiModel;

import java.util.List;

/**
 * AI 模型业务层接口
 */
public interface IAiModelService {

    /** 查询所有模型 */
    List<AiModel> selectModelList();

    /** 根据ID查询模型 */
    AiModel selectModelById(Long id);

    /** 根据模型标识查询已启用的模型 */
    AiModel selectEnabledModel(String modelKey);

    /** 新增模型 */
    int insertModel(AiModel model);

    /** 更新模型 */
    int updateModel(AiModel model);

    /** 删除模型 */
    int deleteModel(Long id);

    /** 启用/禁用模型 */
    int toggleModel(Long id, String enabled);

    /** 测试模型连通性 */
    String testModel(Long id);
}
