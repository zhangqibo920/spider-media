package com.spider.media.framework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson JSON 序列化配置
 *
 * <p>配置全局 ObjectMapper，解决 Java 8 时间类型（LocalDateTime 等）的序列化问题：
 * <ul>
 *   <li>注册 JavaTimeModule 以支持 LocalDateTime、LocalDate 等时间类型的 JSON 序列化/反序列化</li>
 *   <li>禁用 WRITE_DATES_AS_TIMESTAMPS，时间字段以 ISO-8601 字符串格式输出（如 "2024-01-01T12:00:00"）而非时间戳数字</li>
 * </ul></p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置全局 ObjectMapper Bean
     *
     * @return 配置好的 ObjectMapper 实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JavaTimeModule());
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder.build();
    }
}
