package com.spider.media.system.mapper;

import com.spider.media.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户 Mapper 接口
 *
 * <p>定义对 sys_user 表的数据库访问操作，包括用户查询、新增、更新、删除。
 * 所有方法由 MyBatis 通过 XML 映射文件生成实现。</p>
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据用户名查询用户（用于登录验证和用户名唯一性检查）
     *
     * @param userName 用户登录账号
     * @return 对应的用户实体，不存在返回 null
     */
    SysUser selectByUserName(@Param("userName") String userName);

    /**
     * 新增系统用户
     *
     * @param user 待插入的用户实体
     * @return 受影响的行数
     */
    int insert(SysUser user);

    /**
     * 查询所有未删除的用户列表
     *
     * @return 用户列表
     */
    List<SysUser> selectList();

    /**
     * 更新用户信息
     *
     * @param user 待更新的用户实体（根据 userId 匹配）
     * @return 受影响的行数
     */
    int update(SysUser user);

    /**
     * 根据用户ID逻辑删除用户
     *
     * @param userId 用户ID
     * @return 受影响的行数
     */
    int deleteById(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户实体，不存在返回 null
     */
    SysUser selectById(@Param("userId") Long userId);

    /**
     * 修改用户密码
     *
     * @param user 包含 userId 和新密码（加密后）的用户实体
     * @return 受影响的行数
     */
    int updatePassword(SysUser user);

    /**
     * 当前用户更新个人信息（仅 nickName, email, phonenumber）
     *
     * @param user 包含 userId 和待更新字段
     * @return 受影响的行数
     */
    int updateProfile(SysUser user);
}
