package com.spider.media.system.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.common.utils.SecurityUtils;
import com.spider.media.framework.security.JwtToken;
import com.spider.media.system.entity.SysUser;
import com.spider.media.system.mapper.SysUserMapper;
import com.spider.media.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户业务层实现类
 *
 * <p>实现用户注册、登录、查询、更新、删除等核心业务逻辑。
 * 注册时检查用户名唯一性并加密密码；登录时验证密码并生成 JWT Token。</p>
 */
@Service
public class SysUserServiceImpl implements ISysUserService {

    /** 用户数据访问对象 */
    private final SysUserMapper userMapper;
    /** JWT Token 工具类，用于生成登录 Token */
    private final JwtToken jwtToken;

    public SysUserServiceImpl(SysUserMapper userMapper, JwtToken jwtToken) {
        this.userMapper = userMapper;
        this.jwtToken = jwtToken;
    }

    /**
     * 用户注册
     *
     * <p>业务规则：
     * <ol>
     *   <li>检查用户名是否已存在，已存在则抛出异常</li>
     *   <li>使用 BCrypt 加密密码</li>
     *   <li>默认角色为 USER，状态为正常</li>
     *   <li>设置创建人和创建时间</li>
     * </ol></p>
     */
    @Override
    public SysUser register(String userName, String password) {
        if (selectUserByUserName(userName) != null) {
            throw new ServiceException(ErrorCodeEnums.SYS_USERNAME_ALREADY_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setNickName(userName);
        user.setPassword(SecurityUtils.encryptPassword(password));
        user.setStatus("0");
        user.setRole("USER");
        user.setCreateBy(userName);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录
     *
     * <p>验证流程：
     * <ol>
     *   <li>检查用户是否存在</li>
     *   <li>验证密码是否匹配</li>
     *   <li>检查账号是否被停用</li>
     *   <li>通过验证后生成 JWT Token</li>
     * </ol></p>
     */
    @Override
    public String login(String userName, String password) {
        SysUser user = selectUserByUserName(userName);
        if (user == null) {
            throw new ServiceException(ErrorCodeEnums.SYS_USER_NOT_FOUND);
        }
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            throw new ServiceException(ErrorCodeEnums.SYS_PASSWORD_ERROR);
        }
        if ("1".equals(user.getStatus())) {
            throw new ServiceException(ErrorCodeEnums.SYS_USER_NOT_FOUND, "用户已停用");
        }
        return jwtToken.createToken(userName, user.getUserId());
    }

    @Override
    public SysUser selectUserByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }

    @Override
    public List<SysUser> selectUserList() {
        return userMapper.selectList();
    }

    /**
     * 更新用户信息（自动填充更新时间）
     */
    @Override
    public int updateUser(SysUser user) {
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.update(user);
    }

    @Override
    public int deleteUser(Long userId) {
        return userMapper.deleteById(userId);
    }
}
