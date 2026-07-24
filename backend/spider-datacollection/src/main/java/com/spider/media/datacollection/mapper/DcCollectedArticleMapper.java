package com.spider.media.datacollection.mapper;

import com.spider.media.datacollection.entity.DcCollectedArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DcCollectedArticleMapper {

    List<DcCollectedArticle> selectPage(@Param("targetAccountId") Long targetAccountId,
                                        @Param("platform") String platform,
                                        @Param("title") String title);

    int insert(DcCollectedArticle article);

    int batchInsert(@Param("list") List<DcCollectedArticle> articles);

    DcCollectedArticle selectByUrl(@Param("url") String url);
}
