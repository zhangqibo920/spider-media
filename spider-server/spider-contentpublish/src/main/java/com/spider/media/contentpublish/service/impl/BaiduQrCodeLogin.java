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
 * 百家号（百度）扫码登录实现
 *
 * <p>通过百度登录页面实现扫码登录。
 * 百度提供扫码登录API，可以获取二维码供用户扫描。</p>
 */
@Service("baiduQrCodeLogin")
public class BaiduQrCodeLogin implements IQrCodeLoginService {

    private static final Logger log = LoggerFactory.getLogger(BaiduQrCodeLogin.class);

    private final WebClient webClient;

    /** 存储二维码状态信息 */
    private final Map<String, QrCodeStatus> qrCodeStatusMap = new ConcurrentHashMap<>();

    public BaiduQrCodeLogin(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    @Override
    public QrCodeInfo getQrCode() {
        try {
            String token = UUID.randomUUID().toString().replace("-", "");

            // 百度登录页面URL
            String loginUrl = "https://passport.baidu.com/v2/?login";
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + loginUrl;

            // 初始化等待扫码状态
            qrCodeStatusMap.put(token, QrCodeStatus.waiting());

            log.info("生成百家号登录二维码, token: {}", token);
            return new QrCodeInfo(token, qrCodeUrl, 300);

        } catch (Exception e) {
            log.error("获取百家号二维码失败", e);
            throw new RuntimeException("获取二维码失败");
        }
    }

    @Override
    public QrCodeStatus pollStatus(String token) {
        QrCodeStatus status = qrCodeStatusMap.get(token);
        if (status == null) {
            return QrCodeStatus.expired();
        }
        return status;
    }

    /**
     * 手动绑定Cookie
     */
    public void bindCookieManually(String token, String cookie) {
        qrCodeStatusMap.put(token, QrCodeStatus.confirmed(cookie));
        log.info("手动绑定百家号Cookie, token: {}", token);
    }
}
