package com.example.ecommerce.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auth Service API",
                version = "1.0",
                description = """
                        REST API for authentication, authorisation, and user identity management.

                        **Public endpoints** (`POST /api/v1/auth/register`, `/login`, `/refresh`) \
                        require no authentication.

                        **Authenticated endpoints** (`/api/v1/auth/logout`, `/api/v1/users/**`, \
                        `/api/v1/admin/**`) require a valid JWT. Click the **Authorize** button and \
                        enter `Bearer <token>` to authenticate requests in this UI.

                        In the standard deployment all traffic flows through the api-gateway \
                        (port 8080), which validates the JWT and injects `X-User-Id`, `X-Username`, \
                        `X-User-Roles`, and `X-User-Permissions` headers before forwarding the \
                        request. Admin endpoints also require the caller to hold the role or \
                        permission listed in each operation's description.\
                        """
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT access token obtained from POST /api/v1/auth/login"
)
public class OpenApiConfig {
}
