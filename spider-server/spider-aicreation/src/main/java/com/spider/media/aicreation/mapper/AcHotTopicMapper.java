package com.spider.media.aicreation.mapper;

import com.spider.media.aicreation.entity.AcHotTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcHotTopicMapper {

    AcHotTopic selectById(@Param("id") Long id);

    List<AcHotTopic> selectByUserId(@Param("userId") Long userId);

    List<AcHotTopic> selectByKeywordId(@Param("keywordId") Long keywordId, @Param("userId") Long userId);

    List<AcHotTopic> selectByFilter(@Param("userId") Long userId,
                                     @Param("keyword") String keyword,
                                     @Param("source") String source,
                                     @Param("minScore") Integer minScore,
                                     @Param("minRelevance") Integer minRelevance,
                                     @Param("sortBy") String sortBy,
                                     @Param("sortOrder") String sortOrder);

    int insert(AcHotTopic topic);

    int deleteByUserId(@Param("userId") Long userId, @Param("platform") String platform);

    int deleteById(@Param("id") Long id);
}
