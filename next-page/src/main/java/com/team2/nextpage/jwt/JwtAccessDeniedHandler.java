package com.team2.nextpage.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  // 403 Forbidden 에러 처리 (필요한 권한이 없는 경우)
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException, ServletException {
    // 응답 컨텐츠 타입 설정 (JSON)
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

    // 에러 메시지 구성
    Map<String, Object> data = new HashMap<>();
    data.put("status", HttpServletResponse.SC_FORBIDDEN);
    data.put("error", "Forbidden");
    data.put("message", "제공된 토큰으로는 접근할 권한이 없습니다.");
    data.put("path", request.getRequestURI());

    // JSON 변환 및 출력
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), data);
  }
}
