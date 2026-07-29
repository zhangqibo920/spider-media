package com.spider.media.system.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.common.utils.SecurityUtils;
import com.spider.media.framework.security.JwtToken;
import com.spider.media.system.entity.SysRole;
import com.spider.media.system.entity.SysUser;
import com.spider.media.system.entity.SysUserRole;
import com.spider.media.system.mapper.SysRoleMapper;
import com.spider.media.system.mapper.SysUserMapper;
import com.spider.media.system.mapper.SysUserRoleMapper;
import com.spider.media.system.service.ISysUserService;
import com.spider.media.system.service.LoginAttemptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户业务层实现类
 *
 * <p>实现用户注册、登录、查询、更新、删除等核心业务逻辑。
 * 注册时检查用户名唯一性并加密密码；登录时验证密码并生成 JWT Token。</p>
 *
 * <p>角色提升规则：注册接口对所有人开放（无管理员存在时需要引导创建首个管理员），
 * 因此当数据库中尚无任何用户时，新注册的用户自动赋予 ADMIN 角色，便于初始化部署；
 * 之后注册的用户均为普通 USER。管理员可通过用户管理接口手动提升其他用户。</p>
 *
 * <p>登录防爆破：通过 {@link LoginAttemptService} 记录失败次数，
 * 连续失败超过阈值后临时锁定账号，避免暴力破解。</p>
 */
@Service
public class SysUserServiceImpl implements ISysUserService {

    /** 用户数据访问对象 */
    private final SysUserMapper userMapper;
    /** JWT Token 工具类，用于生成登录 Token */
    private final JwtToken jwtToken;
    /** 登录失败计数服务，用于防爆破 */
    private final LoginAttemptService loginAttemptService;
    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper userRoleMapper;
    /** 角色 Mapper */
    private final SysRoleMapper roleMapper;

    public SysUserServiceImpl(SysUserMapper userMapper, JwtToken jwtToken,
                               LoginAttemptService loginAttemptService,
                               SysUserRoleMapper userRoleMapper,
                               SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.jwtToken = jwtToken;
        this.loginAttemptService = loginAttemptService;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * 用户注册
     *
     * <p>业务规则：
     * <ol>
     *   <li>检查用户名是否已存在，已存在则抛出异常</li>
     *   <li>使用 BCrypt 加密密码</li>
     *   <li>状态为正常；角色根据是否为首用户判定（首用户=ADMIN，其余=USER）</li>
     *   <li>设置创建人和创建时间</li>
     * </ol></p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(String userName, String password) {
        // 参数基础校验，避免 Map 取值后传入空值导致后续流程异常
        if (userName == null || userName.isBlank()) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "密码不能为空且长度不能少于 6 位");
        }
        if (selectUserByUserName(userName) != null) {
            throw new ServiceException(ErrorCodeEnums.SYS_USERNAME_ALREADY_EXISTS);
        }

        // 数据库尚无用户时，首个注册账号自动成为管理员（便于初始化部署）
        boolean isFirstUser = userMapper.selectList().isEmpty();
        String role = isFirstUser ? "ADMIN" : "USER";

        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setNickName(userName);
        user.setPassword(SecurityUtils.encryptPassword(password));
        user.setStatus("0");
        user.setRole(role);
        user.setCreateBy(userName);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 同步角色到 sys_user_role 表
        SysRole roleEntity = roleMapper.selectByKey(role);
        if (roleEntity != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getUserId());
            ur.setRoleId(roleEntity.getRoleId());
            userRoleMapper.insert(ur);
        }

        // 清空密码后返回，避免返回对象携带哈希（即便 @JsonIgnore 已处理，也杜绝其他序列化路径泄露）
        user.setPassword(null);
        return user;
    }

    /**
     * 用户登录
     *
     * <p>验证流程：
     * <ol>
     *   <li>检查账号是否被锁定（连续失败次数过多）</li>
     *   <li>检查用户是否存在</li>
     *   <li>验证密码是否匹配</li>
     *   <li>检查账号是否被停用</li>
     *   <li>通过验证后生成 JWT Token（携带 role），并清空失败计数</li>
     * </ol></p>
     *
     * <p>防爆破：每次密码错误会调用 {@link LoginAttemptService#recordFailure} 累加失败次数，
     * 达到阈值后账号会被临时锁定（默认 5 分钟），期间拒绝所有登录尝试。</p>
     */
    @Override
    public String login(String userName, String password) {
        if (userName == null || userName.isBlank() || password == null || password.isBlank()) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "用户名或密码不能为空");
        }

        // 1. 账号锁定检查
        if (loginAttemptService.isLocked(userName)) {
            long remaining = loginAttemptService.getRemainingLockTime(userName);
            throw new ServiceException(ErrorCodeEnums.SYS_USER_LOCKED,
                    "账号已被锁定，请 " + remaining + " 秒后重试");
        }

        SysUser user = selectUserByUserName(userName);
        if (user == null) {
            // 用户不存在也记录失败次数，避免通过响应差异枚举用户名
            loginAttemptService.recordFailure(userName);
            // 用户不存在与密码错误统一返回相同提示，避免用户名枚举
            throw new ServiceException(ErrorCodeEnums.SYS_PASSWORD_ERROR);
        }
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            loginAttemptService.recordFailure(userName);
            int failCount = loginAttemptService.getFailCount(userName);
            int remaining = loginAttemptService.getMaxAttempts() - failCount;
            if (remaining > 0) {
                throw new ServiceException(ErrorCodeEnums.SYS_PASSWORD_ERROR,
                        "用户名或密码错误，剩余尝试次数 " + remaining);
            }
            throw new ServiceException(ErrorCodeEnums.SYS_USER_LOCKED,
                    "失败次数过多，账号已被锁定 " + 5 + " 分钟");
        }
        if ("1".equals(user.getStatus())) {
            throw new ServiceException(ErrorCodeEnums.SYS_USER_NOT_FOUND, "用户已停用");
        }

        // 登录成功，清空失败计数
        loginAttemptService.recordSuccess(userName);
        return jwtToken.createToken(userName, user.getUserId(), user.getRole());
    }

    @Override
    public SysUser selectUserByUserName(String userName) {
        return userMapper.selectByUserName(userName);
    }

    @Override
    public SysUser selectByUserId(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public List<SysUser> selectUserList() {
        return userMapper.selectList();
    }

    /**
     * 更新用户信息（自动填充更新时间）
     *
     * @throws ServiceException 当尝试修改自身角色/状态导致失去管理员权限时抛出，避免管理员误锁自己
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUser user) {
        if (user.getUserId() == null) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "用户ID不能为空");
        }
        // 防止管理员误降级自己角色或停用自己的账号，导致系统无管理员
        String currentUsername = SecurityUtils.getUsername();
        SysUser target = userMapper.selectById(user.getUserId());
        if (target != null && target.getUserName().equals(currentUsername)) {
            if (user.getRole() != null && !"ADMIN".equals(user.getRole())) {
                throw new ServiceException(ErrorCodeEnums.FORBIDDEN, "不能取消当前登录管理员的管理员权限");
            }
            if ("1".equals(user.getStatus())) {
                throw new ServiceException(ErrorCodeEnums.FORBIDDEN, "不能停用当前登录的管理员账号");
            }
        }
        user.setUpdateTime(LocalDateTime.now());
        int rows = userMapper.update(user);

        // 同步角色变更到 sys_user_role 表
        if (user.getRole() != null) {
            SysRole roleEntity = roleMapper.selectByKey(user.getRole());
            if (roleEntity != null) {
                userRoleMapper.deleteByUserId(user.getUserId());
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getUserId());
                ur.setRoleId(roleEntity.getRoleId());
                userRoleMapper.insert(ur);
            }
        }

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUser(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "用户ID不能为空");
        }
        // 禁止管理员删除自己，避免误操作导致系统失去管理员
        SysUser target = userMapper.selectById(userId);
        if (target != null && target.getUserName().equals(SecurityUtils.getUsername())) {
            throw new ServiceException(ErrorCodeEnums.FORBIDDEN, "不能删除当前登录的管理员账号");
        }
        return userMapper.deleteById(userId);
    }

    /**
     * 重置用户密码（管理员操作，无需旧密码）
     *
     * @throws ServiceException 用户不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetPassword(Long userId, String newPassword) {
        if (userId == null) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "用户ID不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "新密码长度不能少于 6 位");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ErrorCodeEnums.SYS_USER_NOT_FOUND);
        }
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updatePassword(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProfile(SysUser user) {
        if (user.getUserId() == null) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "用户ID不能为空");
        }
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateProfile(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "新密码长度不能少于 6 位");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ErrorCodeEnums.SYS_USER_NOT_FOUND);
        }
        if (!SecurityUtils.matchesPassword(oldPassword, user.getPassword())) {
            throw new ServiceException(ErrorCodeEnums.SYS_PASSWORD_ERROR, "旧密码错误");
        }
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updatePassword(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserRole(Long userId) {
        List<SysUserRole> existing = userRoleMapper.selectByUserId(userId);
        if (!existing.isEmpty()) {
            return;
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole().isBlank()) {
            return;
        }
        SysRole roleEntity = roleMapper.selectByKey(user.getRole());
        if (roleEntity != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleEntity.getRoleId());
            userRoleMapper.insert(ur);
        }
    }
}
