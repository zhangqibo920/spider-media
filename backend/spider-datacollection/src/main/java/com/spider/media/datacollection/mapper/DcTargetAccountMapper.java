package com.spider.media.datacollection.mapper;

import com.spider.media.datacollection.entity.DcTargetAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对标账号 Mapper 接口
 *
 * <p>定义对 dc_target_account 表的数据库访问操作，
 * 支持多条件筛选查询、新增、逻辑删除。</p>
 */
@Mapper
public interface DcTargetAccountMapper {

    /**
     * 多条件筛选查询对标账号列表
     *
     * @param userId    用户ID（必传，按当前用户隔离数据）
     * @param platform  平台类型筛选（可为 null 表示不筛选）
     * @param groupName 分组名称筛选（可为 null 表示不筛选）
     * @return 对标账号列表
     */
    List<DcTargetAccount> selectList(@Param("userId") Long userId,
                                     @Param("platform") String platform,
                                     @Param("groupName") String groupName);

    /**
     * 新增对标账号
     *
     * @param account 待插入的对标账号实体
     * @return 受影响的行数
     */
    int insert(DcTargetAccount account);

    /**
     * 根据主键ID逻辑删除对标账号
     *
     * @param id 对标账号主键ID
     * @return 受影响的行数
     */
    int deleteById(@Param("id") Long id);
}
