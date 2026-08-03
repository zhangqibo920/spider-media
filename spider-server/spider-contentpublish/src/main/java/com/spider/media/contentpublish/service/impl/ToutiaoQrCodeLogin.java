package com.spider.media.contentpublish.service.impl;

import com.spider.media.contentpublish.service.IQrCodeLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 头条号扫码登录实现
 *
 * <p>通过头条号登录页面实现扫码登录。
 * 由于头条号没有公开的扫码登录API，这里提供两种方式：
 * 1. 打开头条号登录页面，用户手动扫码后从浏览器获取Cookie
 * 2. 通过OCR识别二维码让用户用头条APP扫码</p>
 */
@Service("toutiaoQrCodeLogin")
public class ToutiaoQrCodeLogin implements IQrCodeLoginService {

    private static final Logger log = LoggerFactory.getLogger(ToutiaoQrCodeLogin.class);

    private final WebClient webClient;

    /** 存储二维码状态信息 */
    private final Map<String, QrCodeStatus> qrCodeStatusMap = new ConcurrentHashMap<>();

    public ToutiaoQrCodeLogin(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    @Override
    public QrCodeInfo getQrCode() {
        try {
            String token = UUID.randomUUID().toString().replace("-", "");

            // 头条号登录页面URL
            String loginUrl = "https://sso.toutiao.com/login/";
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + loginUrl;

            // 初始化等待扫码状态
            qrCodeStatusMap.put(token, QrCodeStatus.waiting());

            log.info("生成头条号登录二维码, token: {}", token);
            return new QrCodeInfo(token, qrCodeUrl, 300);

        } catch (Exception e) {
            log.error("获取头条号二维码失败", e);
            throw new RuntimeException("获取二维码失败");
        }
    }

    @Override
    public QrCodeStatus pollStatus(String token) {
        QrCodeStatus status = qrCodeStatusMap.get(token);
        if (status == null) {
            return QrCodeStatus.expired();
        }

        // 如果是等待状态，返回当前状态（实际项目中可能需要异步更新）
        // 这里提供一个手动确认的方式，用户可以从浏览器获取Cookie后手动绑定
        return status;
    }

    /**
     * 手动绑定Cookie
     * 用户从浏览器获取Cookie后调用此方法
     */
    public void bindCookieManually(String token, String cookie) {
        qrCodeStatusMap.put(token, QrCodeStatus.confirmed(cookie));
        log.info("手动绑定头条号Cookie, token: {}", token);
    }

    /**
     * 清理过期的二维码状态
     */
    public void cleanExpiredStatus() {
        // 实际项目中应该使用定时任务清理
        // 这里简化处理
    }
}
