package com.team2.commonmodule.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

/**
 * MSA 환경에서 Gateway를 통해 전달된 사용자 정보를 편리하게 가져오는 유틸리티 클래스
 *
 * Gateway에서 JWT 검증 후 다음 헤더를 설정합니다:
 * - X-User-Id: 사용자 ID
 * - X-User-Email: 사용자 이메일
 * - X-User-Nickname: 사용자 닉네임
 * - X-User-Role: 사용자 역할 (USER, ADMIN)
 *
 * @author 정진호 (MSA 전환 수정)
 */
public class SecurityUtil {

    private SecurityUtil() {
        // 유틸리티 클래스이므로 인스턴스 생성 방지
    }

    // Gateway에서 설정하는 헤더 키 상수
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_NICKNAME = "X-User-Nickname";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * 현재 HTTP 요청 객체를 가져옵니다.
     *
     * @return HttpServletRequest (없으면 null)
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Gateway에서 전달된 사용자 ID를 반환합니다.
     *
     * @return 사용자 ID (헤더가 없으면 null)
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String userIdHeader = request.getHeader(HEADER_USER_ID);
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getCurrentUserEmail() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String email = request.getHeader(HEADER_USER_EMAIL);
            if (email != null && !email.isEmpty()) {
                return email;
            }
        }

        // Fallback: SecurityContextHolder (Direct Request)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }

        return null;
    }

    /**
     * Gateway에서 전달된 사용자 닉네임을 반환합니다.
     *
     * @return 사용자 닉네임 (헤더가 없으면 null)
     */
    public static String getCurrentUserNickname() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(HEADER_USER_NICKNAME);
    }

    /**
     * Gateway에서 전달된 사용자 역할을 반환합니다.
     *
     * @return 사용자 역할 (USER, ADMIN 등, 헤더가 없으면 null)
     */
    public static String getCurrentUserRole() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String role = request.getHeader(HEADER_USER_ROLE);
            if (role != null && !role.isEmpty()) {
                return role;
            }
        }

        // Fallback: SecurityContextHolder (Direct Request)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     * X-User-Id 헤더가 존재하면 인증된 것으로 간주합니다.
     *
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    /**
     * 현재 사용자가 관리자(ADMIN)인지 확인합니다.
     *
     * @return 관리자 여부
     */
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return role != null && (role.equals("ADMIN") || role.equals("ROLE_ADMIN"));
    }

    /**
     * 현재 사용자가 특정 사용자인지 확인합니다.
     *
     * @param userId 비교할 사용자 ID
     * @return 일치 여부
     */
    public static boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * 현재 사용자가 특정 사용자이거나 관리자인지 확인합니다.
     *
     * @param userId 비교할 사용자 ID
     * @return 권한 여부
     */
    public static boolean isCurrentUserOrAdmin(Long userId) {
        return isCurrentUser(userId) || isAdmin();
    }
}
