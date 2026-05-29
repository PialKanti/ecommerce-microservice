package com.example.ecommerce.gateway.config;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.enums.PermissionCode;
import com.example.ecommerce.commons.enums.RoleCode;
import com.example.ecommerce.gateway.security.AuthenticatedUser;
import com.example.ecommerce.gateway.security.filter.JwtAuthenticationFilter;
import com.example.ecommerce.gateway.security.filter.UserContextPropagationFilter;
import com.example.ecommerce.gateway.security.handler.GatewayAccessDeniedHandler;
import com.example.ecommerce.gateway.security.handler.GatewayAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserContextPropagationFilter userContextFilter;
    private final GatewayAuthenticationEntryPoint authEntryPoint;
    private final GatewayAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userContextFilter, JwtAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // Public auth endpoints
                        .requestMatchers(HttpMethod.POST,
                                ApiEndpoints.Auth.BASE_AUTH + "/login",
                                ApiEndpoints.Auth.BASE_AUTH + "/register",
                                ApiEndpoints.Auth.BASE_AUTH + "/refresh").permitAll()

                        // Swagger & actuator (open in dev)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health").permitAll()

                        // Admin: role management
                        .requestMatchers(HttpMethod.GET, ApiEndpoints.Admin.BASE_ADMIN_ROLES + "/**")
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_ROLE_READ))
                        .requestMatchers(HttpMethod.POST, ApiEndpoints.Admin.BASE_ADMIN_ROLES)
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_ROLE_CREATE))
                        .requestMatchers(HttpMethod.PUT, ApiEndpoints.Admin.BASE_ADMIN_ROLES + "/**")
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_ROLE_UPDATE))
                        .requestMatchers(HttpMethod.DELETE, ApiEndpoints.Admin.BASE_ADMIN_ROLES + "/**")
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_ROLE_DELETE))

                        // Admin: permission management
                        .requestMatchers(HttpMethod.GET, ApiEndpoints.Admin.BASE_ADMIN_PERMISSIONS + "/**")
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_READ))
                        .requestMatchers(HttpMethod.POST, ApiEndpoints.Admin.BASE_ADMIN_PERMISSIONS)
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_CREATE))
                        .requestMatchers(HttpMethod.PUT, ApiEndpoints.Admin.BASE_ADMIN_PERMISSIONS + "/**")
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_UPDATE))
                        .requestMatchers(HttpMethod.DELETE, ApiEndpoints.Admin.BASE_ADMIN_PERMISSIONS + "/**")
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_DELETE))

                        // Admin: user-role assignment
                        .requestMatchers(ApiEndpoints.Admin.BASE_ADMIN_USERS + "/**")
                        .access(roleAndPermission(RoleCode.ADMIN, PermissionCode.PERMISSION_ROLE_ASSIGN))

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .build();
    }

    /**
     * Prevents Spring Boot from registering JwtAuthenticationFilter in the servlet filter chain.
     * It is added to the Spring Security chain via addFilterBefore() above.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Prevents Spring Boot from registering UserContextPropagationFilter in the servlet filter chain.
     */
    @Bean
    public FilterRegistrationBean<UserContextPropagationFilter> userContextFilterRegistration(
            UserContextPropagationFilter filter) {
        FilterRegistrationBean<UserContextPropagationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Requires the principal to have both the given role (ROLE_ prefix added automatically)
     * and the given authority (permission code).
     */
    private AuthorizationManager<RequestAuthorizationContext> roleAndPermission(RoleCode role, PermissionCode permission) {
        return (authSupplier, ctx) -> {
            Authentication auth = authSupplier.get();
            if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof AuthenticatedUser)) {
                return new AuthorizationDecision(false);
            }
            boolean hasRole = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
            boolean hasPermission = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(permission.name()));
            return new AuthorizationDecision(hasRole && hasPermission);
        };
    }
}
