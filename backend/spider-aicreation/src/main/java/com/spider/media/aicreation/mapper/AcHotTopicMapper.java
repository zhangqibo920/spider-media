package com.spider.media.aicreation.mapper;

import com.spider.media.aicreation.entity.AcHotTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcHotTopicMapper {

    AcHotTopic selectById(@Param("id") Long id);

    List<AcHotTopic> selectByUserId(@Param("userId") Long userId);

    int insert(AcHotTopic topic);

    int deleteByUserId(@Param("userId") Long userId, @Param("platform") String platform);
}
