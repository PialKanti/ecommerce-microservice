package com.example.ecommerce.cart.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cart Service API",
                version = "1.0",
                description = """
                        REST API for managing the current user's shopping cart.

                        Every route under `/api/v1/cart/**` requires an authenticated user — there are \
                        no public, unauthenticated endpoints in this service. All traffic flows through \
                        the api-gateway (port 8080), which validates the JWT and injects `X-User-Id` \
                        before forwarding. This service trusts that propagated header; it does **not** \
                        validate JWTs itself.\
                        """
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
