package com.zjnu.studybuddy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI studyBuddyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("学习搭子 StudyBuddy 接口文档")
                        .version("v2.0")
                        .description("""
                                《团队规范项目实训》00931 · 方野 20243190415

                                实验一~五 + 功能增强版接口文档。
                                调试受保护接口时，请先用 /api/auth/login 取得 token，
                                再点击右上角 Authorize 填入令牌（无需手写 Bearer 前缀）。
                                """)
                        .contact(new Contact().name("方野").email("fangye@zjnu.edu.cn"))
                        .license(new License().name("课程实践用途")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("在 /api/auth/login 获取 JWT 后填入此处")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
