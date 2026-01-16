package com.team2.storyservice.config;

import com.team2.commonmodule.filter.GatewayAuthenticationFilter;
import com.team2.commonmodule.filter.JwtToHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Story Service의 Spring Security 설정
 * Gateway로부터 전달된 헤더 기반 인증 처리
 *
 * @author 정진호
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtToHeaderFilter jwtToHeaderFilter;
    private final GatewayAuthenticationFilter gatewayAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT/헤더 기반 인증)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 정책: STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 엔드포인트별 인증/인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // Swagger/OpenAPI 문서 접근 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**")
                        .permitAll()

                        // Actuator, H2 Console
                        .requestMatchers("/actuator/**", "/h2-console/**")
                        .permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // 에러 및 정적 리소스
                        .requestMatchers("/error", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/static/**")
                        .permitAll()

                        // 소설 조회 API - GET 요청만 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/books").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/{bookId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/{bookId}/view").permitAll()

                        // 카테고리 조회 API - GET 요청만 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // 내부 MSA 서비스 간 통신 API - 인증 불필요
                        .requestMatchers("/internal/**").permitAll()

                        // 나머지 모든 요청은 인증 필요 (POST, PUT, DELETE 등)
                        .anyRequest().authenticated())

                // Form Login / HTTP Basic 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // JWT를 헤더로 변환하는 필터 (가장 먼저 실행)
                .addFilterBefore(
                        jwtToHeaderFilter,
                        UsernamePasswordAuthenticationFilter.class)

                // Gateway 헤더 인증 필터 (JwtToHeaderFilter 다음에 실행)
                .addFilterBefore(
                        gatewayAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                // H2 Console 사용을 위한 설정
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
