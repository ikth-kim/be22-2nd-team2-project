package com.team2.gatewayserver.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gateway JWT Authentication Filter
 * JWT 토큰을 검증하고 사용자 정보를 헤더로 주입
 *
 * @author Next-Page Team
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/refresh",
            "/api/auth/check-email",
            "/api/auth/check-nickname",
            "/api/auth/admin",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/favicon.ico",
            "/error");

    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/books",
            "/api/categories",
            "/api/reactions");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        var method = exchange.getRequest().getMethod();

        log.debug("Gateway Filter - Path: {}, Method: {}", path, method);

        // 1. 완전히 제외되는 경로 (로그인, 회원가입 등)
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            log.debug("Gateway Filter - Excluded path, passing through: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.debug("Gateway Filter - Authorization header: {}", authHeader != null ? "present" : "absent");

        // 2. 토큰이 있는 경우: 검증 및 정보 주입 (경로 상관없이 토큰이 오면 검증)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject(); // 토큰의 Subject에서 userId 추출
                String email = claims.get("email", String.class);
                String nickname = claims.get("nickname", String.class);
                String role = claims.get("role", String.class);

                if (userId == null || userId.isEmpty()) {
                    log.error("Gateway Filter - UserId (Subject) is missing in token! Claims: {}", claims);
                    return onError(exchange, "Invalid Token: User ID missing", HttpStatus.UNAUTHORIZED);
                }

                log.info("Gateway Filter - JWT validated successfully for user: {} ({})", email, userId);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-User-Nickname", nickname)
                        .header("X-User-Role", role)
                        .build();

                log.debug("Gateway Filter - Injected headers - X-User-Id: {}, X-User-Email: {}, X-User-Role: {}",
                        userId, email, role);

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException e) {
                // 토큰이 잘못된 경우, 공개 경로라도 401 반환 (보안상 명확함)
                log.warn("Gateway Filter - Invalid JWT token: {}", e.getMessage());
                return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        }

        // 3. 토큰이 없는 경우: 공개 GET 경로인지 확인
        if (method.matches("GET") && PUBLIC_GET_PATHS.stream().anyMatch(path::startsWith)) {
            log.debug("Gateway Filter - Public GET path, allowing anonymous access: {}", path);
            return chain.filter(exchange); // 익명 접근 허용
        }

        // 4. 그 외: 인증 실패
        log.warn("Gateway Filter - No valid token for protected path: {}", path);
        return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorResponse = String.format(
                "{\"success\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"%s\"}",
                message);

        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
