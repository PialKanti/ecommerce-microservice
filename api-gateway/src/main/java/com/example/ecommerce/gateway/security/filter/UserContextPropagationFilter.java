package com.example.ecommerce.gateway.security.filter;

import com.example.ecommerce.gateway.constants.GatewayHeaders;
import com.example.ecommerce.gateway.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Propagates authenticated user context to downstream services via HTTP headers.
 * Must run after JwtAuthenticationFilter so the SecurityContext is already populated.
 */
@Component
@RequiredArgsConstructor
public class UserContextPropagationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof AuthenticatedUser user) {

            MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);

            if (user.id() != null) {
                mutableRequest.addHeader(GatewayHeaders.USER_ID, String.valueOf(user.id()));
            }
            mutableRequest.addHeader(GatewayHeaders.USERNAME, user.username());

            String roles = user.authorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .collect(Collectors.joining(","));
            mutableRequest.addHeader(GatewayHeaders.USER_ROLES, roles);

            String permissions = user.authorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> !a.startsWith("ROLE_"))
                    .collect(Collectors.joining(","));
            mutableRequest.addHeader(GatewayHeaders.USER_PERMISSIONS, permissions);

            filterChain.doFilter(mutableRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
