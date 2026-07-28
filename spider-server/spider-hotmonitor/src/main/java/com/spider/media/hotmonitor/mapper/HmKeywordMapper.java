package com.spider.media.hotmonitor.mapper;

import com.spider.media.hotmonitor.entity.HmKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HmKeywordMapper {

    HmKeyword selectById(@Param("id") Long id);

    List<HmKeyword> selectByUserId(@Param("userId") Long userId);

    List<HmKeyword> selectActiveKeywords(@Param("now") LocalDateTime now);

    int insert(HmKeyword keyword);

    int updateById(HmKeyword keyword);

    int deleteById(@Param("id") Long id);

    int updateLastFetchTime(@Param("id") Long id, @Param("fetchTime") LocalDateTime fetchTime);
}
