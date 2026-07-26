package com.spider.media.framework.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * WebClient 配置类
 *
 * <p>统一配置项目内所有 WebClient 实例的连接超时、读取超时和写入超时，
 * 避免在 AI 调用、爬虫抓取等场景下因网络异常导致线程长时间阻塞。</p>
 *
 * <p>超时策略：
 * <ul>
 *   <li>连接超时：10 秒（防止 DNS 解析或 TCP 握手无限等待）</li>
 *   <li>读取超时：30 秒（AI 大模型生成可能较慢，给足够时间）</li>
 *   <li>写入超时：10 秒</li>
 * </ul></p>
 *
 * <p>通过依赖注入 WebClient.Builder，各业务模块可基于此构建专属 WebClient，
 * 添加自定义 Header（如 User-Agent）。</p>
 */
@Configuration
public class WebClientConfig {

    /** 连接超时（毫秒） */
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    /** 读取超时（秒）—— AI 接口可能较慢 */
    private static final int READ_TIMEOUT_SEC = 30;

    /** 写入超时（秒） */
    private static final int WRITE_TIMEOUT_SEC = 10;

    /**
     * 配置 WebClient.Builder，所有 WebClient 实例通过此 Builder 创建
     *
     * @return 配置好超时的 WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_SEC, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    /**
     * 默认 WebClient Bean，可直接注入使用
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
