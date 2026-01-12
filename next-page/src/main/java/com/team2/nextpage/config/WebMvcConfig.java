package com.team2.nextpage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC 설정
 * CORS, 리소스 핸들러, 인터셉터 등 웹 관련 설정을 담당
 *
 * @author 정진호
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
        private String corsAllowedOrigins;

        /**
         * CORS (Cross-Origin Resource Sharing) 설정
         * 프론트엔드 개발 시 다른 도메인에서의 API 호출을 허용
         */
        @Override
        public void addCorsMappings(CorsRegistry registry) {
                String[] origins = corsAllowedOrigins.split(",");

                registry.addMapping("/api/**")
                                .allowedOrigins(origins)
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                                .allowedHeaders("*")
                                .allowCredentials(true)
                                .maxAge(3600);

                // Swagger UI CORS 설정
                registry.addMapping("/swagger-ui/**")
                                .allowedOrigins("*")
                                .allowedMethods("GET")
                                .allowedHeaders("*");

                registry.addMapping("/v3/api-docs/**")
                                .allowedOrigins("*")
                                .allowedMethods("GET")
                                .allowedHeaders("*");
        }

        /**
         * 정적 리소스 핸들러 설정
         * 이미지, CSS, JS 등 정적 파일 경로 매핑
         */
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Swagger UI 정적 리소스
                registry.addResourceHandler("/swagger-ui/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                                .resourceChain(false);

                // 정적 파일 업로드 디렉토리 (추후 이미지 업로드 기능 구현 시)
                registry.addResourceHandler("/uploads/**")
                                .addResourceLocations("file:./uploads/");

                // 기본 정적 리소스
                registry.addResourceHandler("/static/**")
                                .addResourceLocations("classpath:/static/");
        }

        /**
         * 인터셉터 설정 (필요 시 활성화)
         * 로깅, 성능 측정 등을 위한 인터셉터를 추가할 수 있습니다.
         */
        /*
         * @Override
         * public void addInterceptors(InterceptorRegistry registry) {
         * registry.addInterceptor(new LoggingInterceptor())
         * .addPathPatterns("/api/**")
         * .excludePathPatterns("/api/auth/**");
         * }
         */
}
