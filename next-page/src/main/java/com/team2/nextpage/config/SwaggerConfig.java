package com.team2.nextpage.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.security.*;
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

        @Value("${app.production-url:https://api.nextpage.com}")
        private String productionUrl;

        private static final String SECURITY_SCHEME_NAME = "bearerAuth";
        private static final String API_VERSION = "1.0.0";

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .info(apiInfo())
                                .servers(serverList())
                                .addSecurityItem(securityRequirement())
                                .components(components())
                                .externalDocs(externalDocumentation());
        }

        private Info apiInfo() {
                return new Info()
                                .title("📚 Next Page API Documentation")
                                .description(buildApiDescription())
                                .version(API_VERSION)
                                .contact(buildContact())
                                .license(buildLicense());
        }

        private String buildApiDescription() {
                return """
                                ## 📚 Next Page - 릴레이 소설 창작 플랫폼

                                **Next Page**는 여러 사용자가 함께 소설을 창작하는 협업형 릴레이 소설 플랫폼입니다.

                                ---

                                ### 🎯 주요 기능

                                #### 1. 🔐 회원 인증 (Authentication)
                                - **회원가입**: 이메일 기반 회원가입 및 실시간 중복 검증
                                - **로그인**: JWT 토큰 기반 인증 (Access Token + Refresh Token)
                                - **관리자 기능**: 관리자 권한을 통한 회원 및 콘텐츠 관리 (강제 탈퇴 등)

                                #### 2. ✍️ 소설 집필 (Writing)
                                - **소설 생성**: 제목, 카테고리, 첫 문장으로 새로운 소설 시작
                                - **릴레이 작성**: Sequence 기반 순차적 문장 이어쓰기
                                - **편집 잠금(Editing Lock)**: WebSocket 기반 실시간 작성 상태 공유 및 동시 편집 방지
                                - **작성 제한**: 연속 작성 방지, **마지막 문장만 수정/삭제 가능(History Protection)**

                                #### 3. 📖 소설 조회 (Reading)
                                - **목록 조회**: 페이징, 정렬, 필터링, 무한 스크롤 지원
                                - **뷰어 모드**: 책 넘김 효과와 함께 완결된 소설 감상
                                - **마이페이지**: 내가 쓴 소설, 문장, 댓글 탭별 모아보기

                                #### 4. ❤️ 반응 (Reactions)
                                - **투표 시스템**: 소설 및 문장에 대한 개추/비추(Thumb Up/Down)
                                - **실시간 댓글**: WebSocket을 통한 댓글 실시간 브로드캐스팅
                                - **계층형 댓글**: 대댓글 구조 지원

                                ---

                                ### 🔑 인증 방법

                                이 API는 JWT Bearer 토큰 인증을 사용합니다.
                                1. `POST /api/auth/login` 으로 토큰 발급.
                                2. 우측 상단 **Authorize** 버튼 클릭.
                                3. `Bearer` 없이 토큰 값만 입력.

                                ---

                                ### 🛠️ 기술 스택
                                - **Core**: Spring Boot 3.5.9, Java 17
                                - **Data**: JPA(Command), MyBatis(Query), MariaDB
                                - **Real-time**: WebSocket (STOMP)
                                - **Docs**: Springdoc OpenAPI
                                """;
        }

        private Contact buildContact() {
                return new Contact()
                                .name("Team Next Page")
                                .email("team-nextpage@example.com")
                                .url("https://github.com/team-nextpage");
        }

        private License buildLicense() {
                return new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html");
        }

        private ExternalDocumentation externalDocumentation() {
                return new ExternalDocumentation()
                                .description("📖 Next Page GitHub Repository")
                                .url("https://github.com/team-nextpage");
        }

        private List<Server> serverList() {
                return List.of(
                                new Server().url(serverUrl).description("🖥️ 로컬 개발 서버"),
                                new Server().url(productionUrl).description("🌐 운영 서버"));
        }

        private Components components() {
                return new Components()
                                .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme())
                                .addSchemas("ApiResponse", buildApiResponseSchema())
                                .addSchemas("ErrorResponse", buildErrorResponseSchema());
        }

        private SecurityScheme buildSecurityScheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT Access Token 입력");
        }

        private Schema<?> buildApiResponseSchema() {
                return new Schema<>()
                                .type("object")
                                .description("공통 API 응답 형식")
                                .addProperty("success", new Schema<>().type("boolean").description("성공 여부"))
                                .addProperty("data", new Schema<>().type("object").description("응답 데이터"))
                                .addProperty("error", new Schema<>().$ref("#/components/schemas/ErrorResponse"));
        }

        private Schema<?> buildErrorResponseSchema() {
                return new Schema<>()
                                .type("object")
                                .description("에러 응답 형식")
                                .addProperty("code", new Schema<>().type("string").description("에러 코드"))
                                .addProperty("message", new Schema<>().type("string").description("에러 메시지"));
        }

        private SecurityRequirement securityRequirement() {
                return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
        }
}
