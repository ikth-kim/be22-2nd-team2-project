package com.team2.nextpage.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  // 401 Unauthorized 에러 처리 (인증되지 않은 사용자가 보호된 리소스에 접근할 때)
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException, ServletException {
    // 응답 컨텐츠 타입 설정 (JSON)
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    // 에러 메시지 구성
    Map<String, Object> data = new HashMap<>();
    data.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    data.put("error", "Unauthorized");
    data.put("message", "인증 정보가 없거나 유효하지 않습니다.");
    data.put("path", request.getRequestURI());

    // JSON 변환 및 출력
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), data);
  }
}

