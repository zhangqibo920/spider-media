package com.spider.media.taskscheduler.mapper;

import com.spider.media.taskscheduler.entity.TsScheduledTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TsScheduledTaskMapper {

    List<TsScheduledTask> selectPage(@Param("userId") Long userId,
                                     @Param("taskName") String taskName,
                                     @Param("status") Integer status);

    int insert(TsScheduledTask task);

    TsScheduledTask selectById(@Param("id") Long id);

    int updateById(TsScheduledTask task);
}
