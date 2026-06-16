package com.example.ecommerce.cart.client;

import com.example.ecommerce.cart.exception.UpstreamServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String upstream = methodKey.contains("ProductServiceClient") ? "Product Service" : "Inventory Service";
        String method = methodKey.substring(methodKey.indexOf('#') + 1, methodKey.indexOf('('));

        return switch (response.status()) {
            case 404 -> new EntityNotFoundException(notFoundMessage(method));
            default  -> new UpstreamServiceException(upstream + " returned HTTP " + response.status());
        };
    }

    private String notFoundMessage(String method) {
        return switch (method) {
            case "getProductById"  -> "Product not found";
            case "getByProductId"  -> "Inventory not found for product";
            default -> "Resource not found on upstream service";
        };
    }
}
