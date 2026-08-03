package com.spider.media.contentpublish.service;

/**
 * 扫码登录服务接口
 *
 * <p>定义各平台扫码登录的核心业务方法，包括获取二维码、轮询扫码状态等。
 * 不同平台实现各自的扫码登录逻辑。</p>
 */
public interface IQrCodeLoginService {

    /**
     * 获取登录二维码
     *
     * @return 二维码信息（包含二维码URL和token）
     */
    QrCodeInfo getQrCode();

    /**
     * 轮询扫码状态
     *
     * @param token 获取二维码时返回的token
     * @return 扫码状态信息
     */
    QrCodeStatus pollStatus(String token);

    /**
     * 二维码信息
     */
    record QrCodeInfo(String token, String qrCodeUrl, int expireSeconds) {}

    /**
     * 扫码状态信息
     */
    record QrCodeStatus(String status, String cookie, String message) {
        public static QrCodeStatus waiting() {
            return new QrCodeStatus("WAITING", null, "等待扫码");
        }

        public static QrCodeStatus scanned(String message) {
            return new QrCodeStatus("SCANNED", null, message);
        }

        public static QrCodeStatus confirmed(String cookie) {
            return new QrCodeStatus("CONFIRMED", cookie, "登录成功");
        }

        public static QrCodeStatus expired() {
            return new QrCodeStatus("EXPIRED", null, "二维码已过期");
        }

        public static QrCodeStatus failed(String message) {
            return new QrCodeStatus("FAILED", null, message);
        }
    }
}
