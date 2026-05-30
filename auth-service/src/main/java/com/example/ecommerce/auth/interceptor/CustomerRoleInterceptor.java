package com.example.ecommerce.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CustomerRoleInterceptor implements HandlerInterceptor {

    private static final String ROLES_HEADER  = "X-User-Roles";
    private static final String REQUIRED_ROLE = "ROLE_CUSTOMER";

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String rolesHeader = request.getHeader(ROLES_HEADER);
        if (rolesHeader == null || Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .noneMatch(REQUIRED_ROLE::equals)) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.FORBIDDEN, "Access denied: ROLE_CUSTOMER required");
            problem.setTitle("Forbidden");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");
            response.getWriter().write(objectMapper.writeValueAsString(problem));
            return false;
        }
        return true;
    }
}
