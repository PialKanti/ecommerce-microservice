package com.example.ecommerce.product.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Product Service API",
                version = "1.0",
                description = """
                        REST API for product catalog and category management.

                        **Public endpoints** (`GET /api/v1/categories/**`, `GET /api/v1/products/**`) \
                        require no authentication.

                        **Admin write endpoints** (`/api/v1/admin/**`) require an authenticated user \
                        identity. All traffic in the standard deployment flows through the api-gateway \
                        (port 8080), which validates the JWT and injects `X-User-Id`, `X-Username`, \
                        `X-User-Roles`, and `X-User-Permissions` headers before forwarding the request. \
                        This service trusts those propagated headers; it does **not** validate JWTs itself.\
                        """
        )
)
public class OpenApiConfig {
}
