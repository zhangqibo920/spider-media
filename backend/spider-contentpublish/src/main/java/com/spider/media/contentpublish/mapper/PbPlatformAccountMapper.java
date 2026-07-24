package com.spider.media.contentpublish.mapper;

import com.spider.media.contentpublish.entity.PbPlatformAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PbPlatformAccountMapper {

    List<PbPlatformAccount> selectByUserId(@Param("userId") Long userId);

    int insert(PbPlatformAccount account);

    int deleteById(@Param("id") Long id);
}
