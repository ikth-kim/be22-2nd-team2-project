package com.team2.commonmodule.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT 토큰을 파싱하여 Gateway 헤더 형식으로 변환하는 필터
 * Swagger에서 직접 테스트할 때 JWT 토큰을 헤더로 변환
 *
 * @author 정진호
 */
@Slf4j
@Component
public class JwtToHeaderFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_NICKNAME = "X-User-Nickname";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    @Value("${jwt.secret:}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        if (StringUtils.hasText(secretKey)) {
            this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            log.info("JWT Secret Key initialized for JwtToHeaderFilter");
        } else {
            log.warn("JWT Secret Key not configured. JWT parsing will be disabled.");
        }
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 이미 Gateway 헤더가 있으면 JWT 파싱하지 않음 (Gateway를 통한 요청)
        if (request.getHeader(HEADER_USER_ID) != null) {
            log.debug("Gateway 헤더가 이미 존재하므로 JWT 파싱 건너뜀");
            filterChain.doFilter(request, response);
            return;
        }

        // JWT Secret Key가 설정되지 않았으면 JWT 파싱하지 않음
        if (key == null) {
            log.debug("JWT Secret Key가 설정되지 않아 JWT 파싱을 건너뜁니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                // JWT 토큰 검증 및 파싱
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // 토큰에서 사용자 정보 추출
                String userId = claims.getSubject(); // subject는 userId
                String userEmail = claims.get("email", String.class);
                String userNickname = claims.get("nickname", String.class);
                String userRole = claims.get("role", String.class);

                // Gateway 헤더 형식으로 변환하여 요청에 추가
                HttpServletRequest wrappedRequest = new HeaderMapRequestWrapper(request);
                ((HeaderMapRequestWrapper) wrappedRequest).addHeader(HEADER_USER_ID, userId);
                ((HeaderMapRequestWrapper) wrappedRequest).addHeader(HEADER_USER_EMAIL, userEmail);
                if (userNickname != null) {
                    ((HeaderMapRequestWrapper) wrappedRequest).addHeader(HEADER_USER_NICKNAME, userNickname);
                }
                if (userRole != null) {
                    ((HeaderMapRequestWrapper) wrappedRequest).addHeader(HEADER_USER_ROLE, userRole);
                }

                log.debug("JWT를 Gateway 헤더로 변환: userId={}, email={}, role={}, URI={}",
                        userId, userEmail, userRole, request.getRequestURI());

                filterChain.doFilter(wrappedRequest, response);
                return;

            } catch (Exception e) {
                log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
            }
        }

        // JWT가 없거나 파싱 실패 시 그대로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 토큰 정보 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
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

    /**
     * 요청에 헤더를 동적으로 추가할 수 있는 Wrapper 클래스
     */
    private static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> headerMap = new HashMap<>();

        public HeaderMapRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public void addHeader(String name, String value) {
            headerMap.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = headerMap.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            names.addAll(headerMap.keySet());
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String headerValue = headerMap.get(name);
            if (headerValue != null) {
                return Collections.enumeration(Collections.singletonList(headerValue));
            }
            return super.getHeaders(name);
        }
    }
}
