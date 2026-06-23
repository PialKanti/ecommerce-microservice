package com.example.ecommerce.cart.client;

import com.example.ecommerce.cart.client.dto.ProductClientResponse;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductClientResponse> getProductById(@PathVariable("id") Long id);
}
