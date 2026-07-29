package com.spider.media.aicreation.mapper;

import com.spider.media.aicreation.entity.AcGeneratedArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 生成文章 Mapper 接口
 *
 * <p>定义对 ac_generated_article 表的数据库访问操作，
 * 支持分页查询、新增、更新文章记录。</p>
 */
@Mapper
public interface AcGeneratedArticleMapper {

    /**
     * 分页查询 AI 生成文章（支持按用户ID、状态、标题筛选）
     *
     * @param userId 用户ID（可为 null 表示不筛选）
     * @param status 生成状态（可为 null）
     * @param title  标题模糊查询关键字（可为 null）
     * @return 文章列表
     */
    List<AcGeneratedArticle> selectPage(@Param("userId") Long userId,
                                        @Param("status") String status,
                                        @Param("title") String title);

    /**
     * 新增 AI 生成文章
     *
     * @param article 待插入的文章实体
     * @return 受影响的行数
     */
    int insert(AcGeneratedArticle article);

    /**
     * 更新 AI 生成文章（根据 id 匹配）
     *
     * @param article 待更新的文章实体
     * @return 受影响的行数
     */
    int update(AcGeneratedArticle article);

    /**
     * 根据 ID 删除 AI 生成文章（软删除）
     *
     * @param id 文章ID
     * @return 受影响的行数
     */
    int deleteById(@Param("id") Long id);
}
