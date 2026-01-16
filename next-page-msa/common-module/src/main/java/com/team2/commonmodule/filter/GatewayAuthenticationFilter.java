package com.team2.commonmodule.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Gateway에서 전달된 사용자 정보 헤더를 기반으로 인증을 처리하는 필터
 *
 * MSA 환경에서 Gateway가 JWT를 검증한 후 다음 헤더를 설정합니다:
 * - X-User-Id: 사용자 ID
 * - X-User-Email: 사용자 이메일
 * - X-User-Nickname: 사용자 닉네임
 * - X-User-Role: 사용자 역할 (USER, ADMIN)
 *
 * @author 정진호
 */
@Slf4j
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Gateway에서 전달된 사용자 정보 헤더 추출
        String userId = request.getHeader(HEADER_USER_ID);
        String userEmail = request.getHeader(HEADER_USER_EMAIL);
        String userRole = request.getHeader(HEADER_USER_ROLE);

        // 사용자 ID와 이메일이 모두 존재하면 인증 정보 설정
        if (StringUtils.hasText(userId) && StringUtils.hasText(userEmail)) {
            try {
                // 권한 설정 (ROLE_ 접두사가 없으면 추가)
                String role = StringUtils.hasText(userRole) ? userRole : "USER";
                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                // Authentication 객체 생성 (principal: email, credentials: null)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userEmail,
                                null,
                                Collections.singletonList(authority)
                        );

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Gateway 헤더로부터 인증 정보 설정: userId={}, email={}, role={}, URI={}",
                        userId, userEmail, role, request.getRequestURI());

            } catch (Exception e) {
                log.error("Gateway 헤더 인증 처리 중 오류 발생: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("Gateway 헤더에 사용자 정보가 없습니다. URI: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Swagger UI 및 정적 리소스에는 필터를 적용하지 않음
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/error");
    }
}
