package com.spider.media.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3 配置类
 *
 * <p>定义 API 文档的元信息和全局安全方案（JWT Bearer Token 认证）。
 * 访问路径：
 * <ul>
 *   <li>Swagger UI: /swagger-ui.html</li>
 *   <li>OpenAPI JSON: /v3/api-docs</li>
 * </ul></p>
 *
 * <p>生产环境通过 application-prod.yml 关闭 springdoc.swagger-ui.enabled 防止泄露 API 结构。</p>
 */
@Configuration
public class SpringDocConfig {

    /** JWT 安全方案名称（在 SecurityRequirement 中引用） */
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * 定义 OpenAPI 文档元信息和全局 JWT 认证方案
     *
     * @return 配置好的 OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SpiderMedia API")
                        .description("SpiderMedia 全媒体运营管理系统 - RESTful 接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SpiderMedia Team")
                                .email("dev@spider-media.com")))
                // 全局认证方案：所有接口默认携带 JWT Token
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .schema(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .description("请输入 JWT Token，格式：Bearer {token}"));
    }
}
