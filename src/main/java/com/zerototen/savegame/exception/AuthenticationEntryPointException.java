package com.zerototen.savegame.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEntryPointException implements
    AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(
            new ObjectMapper().writeValueAsString(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증실패: 로그인이 필요합니다!")
            )
        );
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}