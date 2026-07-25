package com.spider.media.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域（CORS）配置
 *
 * <p>配置允许跨域请求的策略，确保前端开发环境和生产环境都能正常访问后端 API。
 * 通过 OrderedCorsFilter 设置最高优先级，确保 CORS 预检请求（OPTIONS）
 * 在 Spring Security 过滤器链之前处理，避免被安全过滤器拦截。</p>
 */
@Configuration
public class CorsConfig {

    /**
     * 创建 CORS 过滤器 Bean
     *
     * <p>配置允许所有来源、常用 HTTP 方法、所有请求头，并支持携带凭证（Cookie）。
     * 缓存预检请求结果 3600 秒（1小时），减少 OPTIONS 请求次数。</p>
     *
     * @return CORS 过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new OrderedCorsFilter(source);
    }

    /**
     * 有序 CORS 过滤器内部类
     *
     * <p>继承 CorsFilter 并实现 Ordered 接口，确保 CORS 过滤器在 Spring Security
     * Filter Chain 之前执行。优先级为 HIGHEST_PRECEDENCE（最高优先级）。</p>
     */
    private static class OrderedCorsFilter extends CorsFilter implements Ordered {
        public OrderedCorsFilter(UrlBasedCorsConfigurationSource source) {
            super(source);
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
