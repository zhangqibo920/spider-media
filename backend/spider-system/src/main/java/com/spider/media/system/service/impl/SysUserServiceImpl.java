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

/**
 * 用户Service实现
 */
@Service
public class SysUserServiceImpl implements ISysUserService {

    private final SysUserMapper userMapper;
    private final JwtToken jwtToken;

    public SysUserServiceImpl(SysUserMapper userMapper, JwtToken jwtToken) {
        this.userMapper = userMapper;
        this.jwtToken = jwtToken;
    }

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
}
