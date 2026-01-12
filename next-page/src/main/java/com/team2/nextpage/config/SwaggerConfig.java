package com.team2.nextpage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3.0 설정
 * API 문서 자동 생성 및 JWT 인증 테스트 지원
 * 
 * 접속 URL: http://localhost:8080/swagger-ui/index.html
 *
 * @author 정진호
 */
@Configuration
public class SwaggerConfig {

        @Value("${spring.application.name:Next Page}")
        private String applicationName;

        @Value("${app.server-url:http://localhost:8080}")
        private String serverUrl;

        private static final String SECURITY_SCHEME_NAME = "bearerAuth";

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(apiInfo())
                                .servers(serverList())
                                .addSecurityItem(securityRequirement())
                                .components(securitySchemes());
        }

        /**
         * API 기본 정보 설정
         */
        private Info apiInfo() {
                return new Info()
                                .title("Next Page API Documentation")
                                .description("""
                                                ## 📚 Next Page - 릴레이 소설 창작 플랫폼

                                                ### 주요 기능
                                                - 🔐 **회원 인증**: 회원가입, 로그인, JWT 토큰 기반 인증
                                                - ✍️ **집필**: Sequence 제어 기반 릴레이 문장 작성
                                                - 📖 **조회**: 소설 목록, 상세, 검색 및 필터링
                                                - ❤️ **반응**: 좋아요/싫어요, 댓글 작성

                                                ### 인증 방법
                                                1. `/api/auth/login` 엔드포인트로 로그인
                                                2. 응답으로 받은 `accessToken` 복사
                                                3. 우측 상단 🔓 Authorize 버튼 클릭
                                                4. `Bearer {token}` 형식으로 입력 (Bearer 자동 추가됨)

                                                ### 기술 스택
                                                - Spring Boot 3.5.9
                                                - Spring Security + JWT
                                                - JPA (Command) + MyBatis (Query)
                                                - MariaDB
                                                """)
                                .version("1.0.0")
                                .contact(new Contact()
                                                .name("Team Next Page")
                                                .email("team-nextpage@example.com")
                                                .url("https://github.com/team-nextpage"))
                                .license(new License()
                                                .name("Apache 2.0")
                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
        }

        /**
         * 서버 목록 설정
         */
        private List<Server> serverList() {
                Server localServer = new Server()
                                .url(serverUrl)
                                .description("로컬 개발 서버");

                Server productionServer = new Server()
                                .url("https://api.nextpage.com")
                                .description("운영 서버 (배포 후)");

                return List.of(localServer, productionServer);
        }

        /**
         * JWT 인증 스키마 설정
         */
        private Components securitySchemes() {
                SecurityScheme securityScheme = new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT 토큰을 입력하세요. (Bearer 접두사는 자동으로 추가됩니다)");

                return new Components()
                                .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme);
        }

        /**
         * 보안 요구사항 설정
         * 모든 API에 JWT 인증이 필요하다는 것을 명시
         */
        private SecurityRequirement securityRequirement() {
                return new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME);
        }
}
