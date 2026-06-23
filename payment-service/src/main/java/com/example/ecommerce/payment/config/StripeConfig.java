package com.example.ecommerce.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeConfig {

    private String apiKey;
    private String successUrl;
    private String cancelUrl;
    private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }
}
