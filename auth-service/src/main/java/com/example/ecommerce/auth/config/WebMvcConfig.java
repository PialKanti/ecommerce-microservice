package com.example.ecommerce.auth.config;

import com.example.ecommerce.auth.interceptor.CustomerRoleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CustomerRoleInterceptor customerRoleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customerRoleInterceptor)
                .addPathPatterns("/api/v1/users/me", "/api/v1/users/me/**");
    }
}
