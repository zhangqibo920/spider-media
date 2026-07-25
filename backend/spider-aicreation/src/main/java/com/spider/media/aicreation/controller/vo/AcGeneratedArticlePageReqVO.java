package com.spider.media.aicreation.controller.vo;

import com.spider.media.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 生成文章分页查询请求 VO
 *
 * <p>继承 PageParam 获得分页参数，额外支持按用户ID、状态、标题进行筛选查询。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AcGeneratedArticlePageReqVO extends PageParam {

    /** 用户ID（筛选条件） */
    private Long userId;

    /** 生成状态筛选：GENERATING/COMPLETED/FAILED */
    private String status;

    /** 标题模糊查询关键字 */
    private String title;
}
