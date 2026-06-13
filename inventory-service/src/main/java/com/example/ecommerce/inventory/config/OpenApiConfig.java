package com.example.ecommerce.inventory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventory Service API",
                version = "1.0",
                description = """
                        REST API for inventory management.

                        **Public endpoints** (`GET /api/v1/inventory/**`) require no authentication.

                        **Admin endpoints** (`/api/v1/admin/inventory/**`) require an authenticated user \
                        with ADMIN or INVENTORY_MANAGER role. All traffic flows through the api-gateway \
                        (port 8080), which validates the JWT and injects `X-User-Id` before forwarding. \
                        This service trusts those propagated headers; it does **not** validate JWTs itself.\
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
