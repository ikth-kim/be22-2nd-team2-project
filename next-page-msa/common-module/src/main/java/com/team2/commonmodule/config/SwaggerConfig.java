package com.team2.commonmodule.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global Swagger Configuration
 * 모든 마이크로서비스에 공통으로 적용되는 OpenAPI 설정입니다.
 * - JWT 인증 (Bearer Token) 설정 포함
 * - 기본 API 정보 설정
 */
@Configuration
public class SwaggerConfig {

        private static final String SECURITY_SCHEME_NAME = "BearerAuth";

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Next Page MSA API")
                                                .version("v2.0")
                                                .description("Next Page 마이크로서비스 API 명세서 (Common Config)")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                                .components(new Components()
                                                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                                                new SecurityScheme()
                                                                                .name(SECURITY_SCHEME_NAME)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("JWT 토큰을 입력해주세요. (예: eyJhbGci...)")))
                                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                                                .url("http://localhost:8000")
                                                .description("API Gateway"));
        }
}
