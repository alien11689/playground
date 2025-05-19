package dpr.playground.taskprovider.auth;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import dpr.playground.taskprovider.tasks.model.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
class LoginEntryPoint extends BasicAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    LoginEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authEx
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.addHeader("Content-Type", "application/json");
        PrintWriter writer = response.getWriter();
        var error = new ErrorDTO("Invalid user or password");
        writer.println(objectMapper.writeValueAsString(error));
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("Tasks");
        super.afterPropertiesSet();
    }
}
