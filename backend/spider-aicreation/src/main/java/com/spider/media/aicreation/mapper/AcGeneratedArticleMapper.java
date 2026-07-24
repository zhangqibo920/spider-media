package com.spider.media.aicreation.mapper;

import com.spider.media.aicreation.entity.AcGeneratedArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcGeneratedArticleMapper {

    List<AcGeneratedArticle> selectPage(@Param("userId") Long userId,
                                        @Param("status") String status,
                                        @Param("title") String title);

    int insert(AcGeneratedArticle article);
}
