package com.spider.media.contentpublish.mapper;

import com.spider.media.contentpublish.entity.PbPlatformAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台账号 Mapper 接口
 *
 * <p>定义对 pb_platform_account 表的数据库访问操作，由 MyBatis 通过 XML 映射文件生成实现。
 * 所有查询方法默认过滤已逻辑删除的记录（del_flag != '0'）。</p>
 */
@Mapper
public interface PbPlatformAccountMapper {

    /**
     * 根据主键ID查询单个平台账号（仅查询未删除的记录）
     *
     * @param id 账号主键ID
     * @return 对应的平台账号实体，不存在或已删除时返回 null
     */
    PbPlatformAccount selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询该用户下的所有平台账号（仅查询未删除的记录）
     *
     * @param userId 用户ID
     * @return 该用户关联的平台账号列表，按创建时间倒序排列
     */
    List<PbPlatformAccount> selectByUserId(@Param("userId") Long userId);

    /**
     * 新增一条平台账号记录
     *
     * @param account 待插入的平台账号实体（ID由数据库自增生成）
     * @return 受影响的行数，1表示插入成功
     */
    int insert(PbPlatformAccount account);

    /**
     * 根据主键ID逻辑删除平台账号（将 del_flag 设为 '2'）
     *
     * @param id 账号主键ID
     * @return 受影响的行数，1表示删除成功
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新平台账号
     *
     * @param account 待更新的账号实体
     * @return 受影响的行数
     */
    int updateById(PbPlatformAccount account);
}
