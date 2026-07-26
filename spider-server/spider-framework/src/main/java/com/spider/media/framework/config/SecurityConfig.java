package com.spider.media.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.spider.media.framework.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置
 *
 * <p>配置应用的安全策略，包括：
 * <ul>
 *   <li>禁用 CSRF（前后端分离项目不需要，使用 JWT 而非 Cookie）</li>
 *   <li>启用 CORS（按配置项 {@code cors.*} 加载允许的源、方法、头）</li>
 *   <li>使用无状态 Session（STATELESS），每次请求携带 JWT Token 认证</li>
 *   <li>/api/auth/** 路径无需认证（登录、注册接口）</li>
 *   <li>其他所有接口需要认证；管理员接口由 {@code @PreAuthorize} 二次校验角色</li>
 *   <li>在 UsernamePasswordAuthenticationFilter 之前插入 JWT 认证过滤器</li>
 *   <li>未认证请求返回 401 JSON 响应；权限不足返回 403 JSON 响应</li>
 * </ul></p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** JWT 认证过滤器，负责解析和验证请求中的 JWT Token */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** CORS 允许的源，从配置文件读取（多个用逗号分隔） */
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /** CORS 允许的 HTTP 方法 */
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    /** CORS 允许的请求头 */
    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    /** CORS 是否允许携带凭证 */
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    /** CORS 预检请求缓存时间（秒） */
    @Value("${cors.max-age:3600}")
    private long maxAge;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置 Spring Security 过滤器链
     *
     * @param http HttpSecurity 配置器
     * @return 安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                // SpringDoc OpenAPI 接口文档（prod 环境通过配置关闭，这里仅放行未关闭时访问）
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Actuator 健康检查和信息端点放行（监控探针使用）
                // 注意：env、threaddump 等敏感端点已在 application.yml 中通过 management.endpoints.web.exposure 控制
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                // 预检请求（OPTIONS）一律放行，由 CORS 过滤器处理
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"没有权限\",\"data\":null}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 配置源
     *
     * <p>从 application.yml 的 {@code cors.*} 配置项加载允许的源、方法、头。
     * 默认仅允许本地前端开发地址（http://localhost:3000），生产环境通过环境变量
     * {@code CORS_ALLOWED_ORIGINS} 显式指定前端域名，多个用逗号分隔。</p>
     *
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        // "*" 与 allowCredentials=true 不兼容，特殊处理
        if ("*".equals(allowedHeaders.trim())) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        // 暴露自定义响应头给前端 JS（如分页信息、Token 等）
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置密码编码器（BCrypt 算法）
     *
     * @return 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器
     *
     * @param config 认证配置
     * @return 认证管理器实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
