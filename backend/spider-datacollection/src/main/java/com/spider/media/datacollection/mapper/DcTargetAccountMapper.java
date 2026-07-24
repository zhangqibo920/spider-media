package com.spider.media.datacollection.mapper;

import com.spider.media.datacollection.entity.DcTargetAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DcTargetAccountMapper {

    List<DcTargetAccount> selectList(@Param("userId") Long userId,
                                     @Param("platform") String platform,
                                     @Param("groupName") String groupName);

    int insert(DcTargetAccount account);

    int deleteById(@Param("id") Long id);
}
