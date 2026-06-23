package com.example.ecommerce.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "payment.expiration")
@Getter
@Setter
public class PaymentExpirationProperties {

    private Duration lifetime = Duration.ofMinutes(30);
    private Duration checkDelay = Duration.ofSeconds(60);
}
