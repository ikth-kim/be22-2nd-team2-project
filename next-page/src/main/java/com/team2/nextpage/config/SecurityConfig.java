package com.team2.nextpage.config;

import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 * JWT 기반 인증/인가 처리 및 엔드포인트별 접근 권한 관리
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * 비밀번호 암호화를 위한 Encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * 인증 관리자 (로그인 처리용)
   * AuthService에서 로그인 시 사용
   */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  /**
   * Security Filter Chain 설정
   * - JWT 기반 Stateless 세션 관리
   * - 엔드포인트별 접근 권한 제어
   * - CSRF 비활성화 (REST API는 CSRF 불필요)
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 비활성화 (JWT 사용으로 불필요)
        .csrf(AbstractHttpConfigurer::disable)

        // 세션 정책: STATELESS (JWT 사용)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 엔드포인트별 인증/인가 규칙
        .authorizeHttpRequests(auth -> auth
            // 1. 인증 불필요 (Public) - 누구나 접근 가능
            .requestMatchers(
                "/api/auth/**", // 회원가입, 로그인
                "/api/books", // 소설 목록 조회 (GET)
                "/api/books/{bookId}", // 소설 상세 조회 (GET)
                "/api/books/{bookId}/view", // 책 뷰어 모드
                "/error",
                "/favicon.ico")
            .permitAll()

            // 2. Swagger/OpenAPI 문서 접근 허용
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**")
            .permitAll()

            // 3. 개발 도구 (H2 Console, Actuator 등)
            .requestMatchers(
                "/h2-console/**",
                "/actuator/**")
            .permitAll()

            // 4. 인증 필요 (Private) - 로그인한 사용자만
            .anyRequest().authenticated())

        // Form Login / HTTP Basic 비활성화
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)

        // JWT 인증 필터 추가
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class)

        // H2 Console 사용을 위한 설정 (개발 환경)
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin()));

    return http.build();
  }
}