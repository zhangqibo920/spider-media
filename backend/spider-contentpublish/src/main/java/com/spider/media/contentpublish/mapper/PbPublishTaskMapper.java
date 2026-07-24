package com.spider.media.contentpublish.mapper;

import com.spider.media.contentpublish.entity.PbPublishTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PbPublishTaskMapper {

    List<PbPublishTask> selectPage(@Param("userId") Long userId,
                                   @Param("platform") String platform,
                                   @Param("status") Integer status);

    int insert(PbPublishTask task);

    PbPublishTask selectById(@Param("id") Long id);

    int updateById(PbPublishTask task);
}
