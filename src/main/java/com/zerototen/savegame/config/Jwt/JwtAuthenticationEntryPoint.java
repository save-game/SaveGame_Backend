package com.zerototen.savegame.config.Jwt;

import static com.zerototen.savegame.exception.ErrorCode.JWT_TIMEOUT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        //Content-type : application/json;charset=utf-8
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // {"username":"loop-study", "age":20}
        String result = objectMapper.writeValueAsString(JWT_TIMEOUT);
        response.getWriter().write(result);
    }
}
