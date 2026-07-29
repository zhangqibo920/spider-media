package com.spider.media.system.service;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 *
 * <p>基于 Hutool 的 LineCaptcha 生成图形验证码，验证码文本存储在 Redis 中，
 * 通过唯一 captchaId 关联客户端与服务器端的验证码。</p>
 *
 * <p>验证码生命周期：
 * <ol>
 *   <li>客户端调用 /api/auth/captcha 获取 {captchaId, image}</li>
 *   <li>服务器生成验证码文本，存入 Redis（key=captcha:{captchaId}, TTL=5分钟）</li>
 *   <li>客户端在登录请求中携带 captchaId 和 captchaCode</li>
 *   <li>服务器从 Redis 取出验证码文本进行比对，比对后立即删除（一次性使用）</li>
 * </ol></p>
 */
@Service
public class CaptchaService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);

    /** Redis Key 前缀：验证码 */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    /** 验证码有效期（分钟） */
    @Value("${captcha.expire-minutes:5}")
    private long expireMinutes;

    /** 验证码图片宽度 */
    @Value("${captcha.width:120}")
    private int width;

    /** 验证码图片高度 */
    @Value("${captcha.height:40}")
    private int height;

    /** 验证码字符数 */
    @Value("${captcha.code-count:4}")
    private int codeCount;

    /** 干扰线数量 */
    @Value("${captcha.line-count:30}")
    private int lineCount;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成验证码
     *
     * <p>生成图形验证码，将验证码文本存储到 Redis（TTL=5分钟），
     * 返回包含 captchaId 和 Base64 编码图片的 Map。</p>
     *
     * @return Map 包含 captchaId（唯一标识）和 img（Base64 编码的 PNG 图片）
     */
    public Map<String, String> generateCaptcha() {
        // 生成图形验证码（Hutool LineCaptcha）
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(width, height, codeCount, lineCount);
        String code = captcha.getCode();
        String captchaId = IdUtil.fastSimpleUUID();

        // 存储到 Redis，5分钟后自动过期
        try {
            redisTemplate.opsForValue().set(
                    CAPTCHA_KEY_PREFIX + captchaId,
                    code,
                    expireMinutes,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("验证码存储到 Redis 失败", e);
            throw new RuntimeException("验证码服务暂不可用", e);
        }

        // 转换为 Base64
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        captcha.write(bos);
        String base64Image = Base64.getEncoder().encodeToString(bos.toByteArray());
        IoUtil.close(bos);

        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("img", "data:image/png;base64," + base64Image);
        return result;
    }

    /**
     * 校验验证码
     *
     * <p>从 Redis 取出验证码文本与用户输入进行比对（忽略大小写）。
     * 无论校验成功或失败，都会立即从 Redis 删除该验证码，确保一次性使用。</p>
     *
     * @param captchaId  验证码唯一标识
     * @param captchaCode 用户输入的验证码文本
     * @return true=校验通过；false=校验失败（验证码错误或已过期）
     */
    public boolean validateCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            return false;
        }

        String key = CAPTCHA_KEY_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(key);

        // 无论成功失败都删除，防止验证码被复用
        redisTemplate.delete(key);

        if (storedCode == null) {
            return false;
        }
        return storedCode.equalsIgnoreCase(captchaCode);
    }
}
