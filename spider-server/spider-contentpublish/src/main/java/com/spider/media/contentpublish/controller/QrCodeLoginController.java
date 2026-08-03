package com.spider.media.contentpublish.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.service.IPbPlatformAccountService;
import com.spider.media.contentpublish.service.IQrCodeLoginService;
import com.spider.media.contentpublish.service.impl.BaiduQrCodeLogin;
import com.spider.media.contentpublish.service.impl.ToutiaoQrCodeLogin;
import com.spider.media.framework.security.LoginUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 扫码登录控制器
 *
 * <p>提供头条号、百家号等平台的扫码登录接口。
 * 包括获取二维码、轮询扫码状态、手动绑定Cookie等功能。</p>
 */
@RestController
@RequestMapping("/api/publish/qrcode")
public class QrCodeLoginController extends BaseController {

    private final ToutiaoQrCodeLogin toutiaoQrCodeLogin;
    private final BaiduQrCodeLogin baiduQrCodeLogin;
    private final IPbPlatformAccountService platformAccountService;

    public QrCodeLoginController(
            @Qualifier("toutiaoQrCodeLogin") ToutiaoQrCodeLogin toutiaoQrCodeLogin,
            @Qualifier("baiduQrCodeLogin") BaiduQrCodeLogin baiduQrCodeLogin,
            IPbPlatformAccountService platformAccountService) {
        this.toutiaoQrCodeLogin = toutiaoQrCodeLogin;
        this.baiduQrCodeLogin = baiduQrCodeLogin;
        this.platformAccountService = platformAccountService;
    }

    /**
     * 获取头条号登录二维码
     */
    @GetMapping("/toutiao")
    public R<IQrCodeLoginService.QrCodeInfo> getToutiaoQrCode() {
        return ok(toutiaoQrCodeLogin.getQrCode());
    }

    /**
     * 获取百家号登录二维码
     */
    @GetMapping("/baijiahao")
    public R<IQrCodeLoginService.QrCodeInfo> getBaijiahaoQrCode() {
        return ok(baiduQrCodeLogin.getQrCode());
    }

    /**
     * 轮询扫码状态（头条号）
     */
    @GetMapping("/toutiao/poll/{token}")
    public R<IQrCodeLoginService.QrCodeStatus> pollToutiaoStatus(@PathVariable String token) {
        return ok(toutiaoQrCodeLogin.pollStatus(token));
    }

    /**
     * 轮询扫码状态（百家号）
     */
    @GetMapping("/baijiahao/poll/{token}")
    public R<IQrCodeLoginService.QrCodeStatus> pollBaijiahaoStatus(@PathVariable String token) {
        return ok(baiduQrCodeLogin.pollStatus(token));
    }

    /**
     * 手动绑定头条号Cookie
     *
     * <p>由于头条号没有公开的扫码登录API，用户需要：
     * 1. 打开头条号创作平台
     * 2. 登录后从浏览器获取Cookie
     * 3. 将Cookie粘贴到系统中绑定</p>
     */
    @PostMapping("/toutiao/bind")
    public R<Map<String, Object>> bindToutiaoCookie(@RequestBody Map<String, String> request) {
        String cookie = request.get("cookie");
        String accountName = request.get("accountName");

        if (cookie == null || cookie.isBlank()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Cookie不能为空");
            return ok(result);
        }

        Long userId = LoginUser.getUserId();

        // 检查是否已存在同名账号
        PbPlatformAccount existAccount = findExistingAccount(userId, "toutiao", accountName);
        if (existAccount != null) {
            // 更新Cookie
            existAccount.setCookie(cookie);
            existAccount.setLastLoginTime(LocalDateTime.now());
            platformAccountService.updateAccount(existAccount);
        } else {
            // 创建新账号
            PbPlatformAccount account = new PbPlatformAccount();
            account.setUserId(userId);
            account.setPlatform("toutiao");
            account.setAccountName(accountName != null ? accountName : "头条号账号");
            account.setCookie(cookie);
            account.setLastLoginTime(LocalDateTime.now());
            account.setStatus("0");
            platformAccountService.insertAccount(account);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "头条号账号绑定成功");
        return ok(result);
    }

    /**
     * 手动绑定百家号Cookie
     */
    @PostMapping("/baijiahao/bind")
    public R<Map<String, Object>> bindBaijiahaoCookie(@RequestBody Map<String, String> request) {
        String cookie = request.get("cookie");
        String accountName = request.get("accountName");

        if (cookie == null || cookie.isBlank()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Cookie不能为空");
            return ok(result);
        }

        Long userId = LoginUser.getUserId();

        // 检查是否已存在同名账号
        PbPlatformAccount existAccount = findExistingAccount(userId, "baijiahao", accountName);
        if (existAccount != null) {
            // 更新Cookie
            existAccount.setCookie(cookie);
            existAccount.setLastLoginTime(LocalDateTime.now());
            platformAccountService.updateAccount(existAccount);
        } else {
            // 创建新账号
            PbPlatformAccount account = new PbPlatformAccount();
            account.setUserId(userId);
            account.setPlatform("baijiahao");
            account.setAccountName(accountName != null ? accountName : "百家号账号");
            account.setCookie(cookie);
            account.setLastLoginTime(LocalDateTime.now());
            account.setStatus("0");
            platformAccountService.insertAccount(account);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "百家号账号绑定成功");
        return ok(result);
    }

    /**
     * 查找已存在的账号
     */
    private PbPlatformAccount findExistingAccount(Long userId, String platform, String accountName) {
        if (accountName == null || accountName.isBlank()) {
            return null;
        }
        var accounts = platformAccountService.selectAccountList(userId);
        return accounts.stream()
                .filter(a -> platform.equals(a.getPlatform()) && accountName.equals(a.getAccountName()))
                .findFirst()
                .orElse(null);
    }
}
