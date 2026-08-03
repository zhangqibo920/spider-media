package com.spider.media.contentpublish.service.impl;

import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.result.ErrorCodeEnums;
import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.mapper.PbPlatformAccountMapper;
import com.spider.media.contentpublish.service.IPbPlatformAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台账号业务层实现类
 *
 * <p>实现 IPbPlatformAccountService 接口，负责平台账号的增删查操作。
 * 通过构造器注入 PbPlatformAccountMapper，由 MyBatis 执行实际的数据库操作。
 * 新增账号时自动填充创建人和创建时间等审计字段。</p>
 *
 * <p>删除账号时强制校验归属，防止越权删除他人账号。</p>
 */
@Service
public class PbPlatformAccountServiceImpl implements IPbPlatformAccountService {

    private static final Logger log = LoggerFactory.getLogger(PbPlatformAccountServiceImpl.class);

    /** 平台账号数据访问对象，由 Spring 自动注入 */
    private final PbPlatformAccountMapper platformAccountMapper;

    private final WebClient webClient;

    /** 构造器注入 Mapper 依赖 */
    public PbPlatformAccountServiceImpl(PbPlatformAccountMapper platformAccountMapper,
                                         WebClient.Builder webClientBuilder) {
        this.platformAccountMapper = platformAccountMapper;
        this.webClient = webClientBuilder
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    /**
     * 查询指定用户的所有平台账号
     *
     * @param userId 用户ID
     * @return 该用户的平台账号列表，按创建时间倒序
     */
    @Override
    public PbPlatformAccount selectById(Long id) {
        return platformAccountMapper.selectById(id);
    }

    @Override
    public List<PbPlatformAccount> selectAccountList(Long userId) {
        return platformAccountMapper.selectByUserId(userId);
    }

    /**
     * 新增平台账号
     *
     * <p>在插入数据库前，自动设置审计字段：
     * <ul>
     *   <li>createBy - 创建人，使用用户ID作为标识</li>
     *   <li>createTime - 创建时间，使用当前系统时间</li>
     * </ul></p>
     *
     * @param account 待新增的平台账号实体
     * @return 受影响的行数，1表示新增成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAccount(PbPlatformAccount account) {
        account.setCreateBy(String.valueOf(account.getUserId()));
        account.setCreateTime(LocalDateTime.now());
        return platformAccountMapper.insert(account);
    }

    /**
     * 逻辑删除平台账号
     *
     * <p>删除前校验账号归属，仅允许账号所有者删除。</p>
     *
     * @param id     账号主键ID
     * @param userId 当前操作用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAccountById(Long id, Long userId) {
        validateOwnership(id, userId);
        return platformAccountMapper.deleteById(id);
    }

    /**
     * 更新平台账号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAccount(PbPlatformAccount account) {
        Long userId = account.getUserId();
        validateOwnership(account.getId(), userId);
        account.setUpdateBy(String.valueOf(userId));
        account.setUpdateTime(LocalDateTime.now());
        return platformAccountMapper.updateById(account);
    }

    /**
     * 校验平台账号归属
     */
    @Override
    public PbPlatformAccount validateOwnership(Long id, Long userId) {
        PbPlatformAccount account = platformAccountMapper.selectById(id);
        if (account == null) {
            throw new ServiceException(ErrorCodeEnums.PB_ACCOUNT_NOT_FOUND);
        }
        if (userId == null || !account.getUserId().equals(userId)) {
            throw new ServiceException(ErrorCodeEnums.FORBIDDEN, "无权操作他人的平台账号");
        }
        return account;
    }

    /**
     * 测试账号连接是否有效
     */
    @Override
    public boolean testConnection(PbPlatformAccount account) {
        try {
            String platform = account.getPlatform();
            if ("wechat_mp".equals(platform)) {
                return testWechatMpConnection(account);
            } else if ("toutiao".equals(platform) || "baijiahao".equals(platform)) {
                return testCookieConnection(account);
            }
            return false;
        } catch (Exception e) {
            log.warn("账号连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新账号Token
     */
    @Override
    public boolean refreshToken(PbPlatformAccount account) {
        try {
            String platform = account.getPlatform();
            if ("wechat_mp".equals(platform)) {
                return refreshWechatMpToken(account);
            }
            // 头条号、百家号使用Cookie，无需刷新Token
            return false;
        } catch (Exception e) {
            log.warn("Token刷新失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 测试微信公众号连接
     * 使用 AppID 和 AppSecret 获取 Access Token
     */
    private boolean testWechatMpConnection(PbPlatformAccount account) {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    account.getAppId(), account.getAppSecret());

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && response.contains("access_token")) {
                // 更新 Token 信息
                account.setAccessToken(extractJsonValue(response, "access_token"));
                int expiresIn = Integer.parseInt(extractJsonValue(response, "expires_in"));
                account.setTokenExpireTime(LocalDateTime.now().plusSeconds(expiresIn));
                account.setLastLoginTime(LocalDateTime.now());
                platformAccountMapper.updateById(account);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("微信公众号连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 测试Cookie连接（头条号/百家号）
     */
    private boolean testCookieConnection(PbPlatformAccount account) {
        try {
            String cookie = account.getCookie();
            if (cookie == null || cookie.isBlank()) {
                return false;
            }
            // 简单验证Cookie格式
            account.setLastLoginTime(LocalDateTime.now());
            platformAccountMapper.updateById(account);
            return true;
        } catch (Exception e) {
            log.warn("Cookie连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新微信公众号 Access Token
     */
    private boolean refreshWechatMpToken(PbPlatformAccount account) {
        return testWechatMpConnection(account);
    }

    /**
     * 从JSON字符串中提取简单字段值
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            return "";
        }
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
