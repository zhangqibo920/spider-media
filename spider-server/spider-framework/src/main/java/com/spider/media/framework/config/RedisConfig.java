package com.spider.media.framework.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 *
 * <p>自定义 RedisTemplate 的序列化策略：
 * <ul>
 *   <li>Key 使用 StringRedisSerializer，便于在 redis-cli 中查看</li>
 *   <li>Value 使用 Jackson2JsonRedisSerializer，支持对象序列化</li>
 * </ul></p>
 *
 * <p>同时提供 StringRedisTemplate Bean，用于纯字符串场景（如验证码、计数器），
 * 避免对象序列化开销。</p>
 */
@Configuration
public class RedisConfig {

    /**
     * 通用 RedisTemplate，支持任意类型 Value 的 JSON 序列化
     *
     * @param connectionFactory Redis 连接工厂（由 Spring Boot 自动配置）
     * @return 配置好的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 Jackson JSON 序列化
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(buildObjectMapper(), Object.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 字符串专用 RedisTemplate，用于验证码、登录失败计数等纯字符串场景
     *
     * @param connectionFactory Redis 连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * 构建 Jackson ObjectMapper，启用默认类型信息以支持反序列化为具体类型
     */
    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }
}
