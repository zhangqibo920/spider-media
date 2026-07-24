package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysOperLogMapper {

    int insert(SysOperLog log);

    List<SysOperLog> selectPage(@Param("username") String username,
                                 @Param("module") String module,
                                 @Param("pageNo") int pageNo,
                                 @Param("pageSize") int pageSize);

    long selectCount(@Param("username") String username,
                     @Param("module") String module);
}
