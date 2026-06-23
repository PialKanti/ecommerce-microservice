package com.example.ecommerce.payment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Payment Service API",
                version = "1.0",
                description = """
                        REST API for payment processing via Stripe Checkout.

                        Webhook redirect endpoints (`/api/v1/payments/webhook/**`) are public — \
                        Stripe redirects the browser here after the user completes or cancels payment.

                        Admin routes (`/api/v1/admin/payments/**`) require ADMIN or SUPPORT_AGENT role \
                        with the appropriate payment permission.

                        All authenticated traffic flows through the api-gateway (port 8080), which \
                        validates the JWT and injects `X-User-Id` before forwarding.\
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
