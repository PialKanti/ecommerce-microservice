package com.example.ecommerce.order.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Order Service API",
                version = "1.0",
                description = """
                        REST API for order placement, lookup, and cancellation.

                        Customer routes (`/api/v1/orders/**`) require an authenticated user. \
                        Admin routes (`/api/v1/admin/orders/**`) require ADMIN or SUPPORT_AGENT role \
                        with the appropriate order permission.

                        All traffic flows through the api-gateway (port 8080), which validates the JWT \
                        and injects `X-User-Id` before forwarding. This service trusts that propagated \
                        header; it does **not** validate JWTs itself.\
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
