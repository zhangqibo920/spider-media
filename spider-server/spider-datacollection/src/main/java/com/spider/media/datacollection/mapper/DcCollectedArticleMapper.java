package com.spider.media.datacollection.mapper;

import com.spider.media.datacollection.entity.DcCollectedArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采集文章 Mapper 接口
 *
 * <p>定义对 dc_collected_article 表的数据库访问操作，
 * 支持分页查询、新增、批量新增、按 URL 去重查询。</p>
 */
@Mapper
public interface DcCollectedArticleMapper {

    /**
     * 分页查询采集文章（支持按对标账号ID、平台、标题筛选）
     *
     * @param targetAccountId 对标账号ID（可为 null 表示不筛选）
     * @param platform        平台类型（可为 null）
     * @param title           标题模糊查询关键字（可为 null）
     * @return 文章列表
     */
    List<DcCollectedArticle> selectPage(@Param("targetAccountId") Long targetAccountId,
                                        @Param("platform") String platform,
                                        @Param("title") String title);

    /**
     * 新增单篇采集文章
     *
     * @param article 待插入的文章实体
     * @return 受影响的行数
     */
    int insert(DcCollectedArticle article);

    /**
     * 批量新增采集文章（提高采集效率）
     *
     * @param articles 待插入的文章列表
     * @return 受影响的总行数
     */
    int batchInsert(@Param("list") List<DcCollectedArticle> articles);

    /**
     * 根据文章 URL 查询是否已存在（用于去重，避免重复采集）
     *
     * @param url 文章原始链接
     * @return 已存在的文章实体，不存在返回 null
     */
    DcCollectedArticle selectByUrl(@Param("url") String url);

    /**
     * 逻辑删除单篇采集文章
     *
     * @param id 文章ID
     * @return 受影响的行数
     */
    int deleteById(@Param("id") Long id);
}
