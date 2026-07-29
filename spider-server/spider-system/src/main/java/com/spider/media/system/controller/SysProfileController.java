package com.spider.media.system.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.system.entity.SysUser;
import com.spider.media.system.service.ISysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
public class SysProfileController extends BaseController {

    private final ISysUserService userService;

    public SysProfileController(ISysUserService userService) {
        this.userService = userService;
    }

    @PutMapping
    public R<Void> updateProfile(@RequestBody SysUser user) {
        String username = LoginUser.getUsername();
        SysUser currentUser = userService.selectUserByUserName(username);
        if (currentUser == null) {
            throw new ServiceException(ErrorCodeEnums.SYS_USER_NOT_FOUND);
        }
        user.setUserId(currentUser.getUserId());
        user.setUpdateBy(username);
        userService.updateProfile(user);
        return ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new ServiceException(ErrorCodeEnums.PARAM_ERROR, "旧密码不能为空");
        }
        String username = LoginUser.getUsername();
        SysUser currentUser = userService.selectUserByUserName(username);
        if (currentUser == null) {
            throw new ServiceException(ErrorCodeEnums.SYS_USER_NOT_FOUND);
        }
        userService.changePassword(currentUser.getUserId(), oldPassword, newPassword);
        return ok();
    }
}
