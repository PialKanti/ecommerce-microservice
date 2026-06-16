package com.example.ecommerce.inventory.client;

import com.example.ecommerce.commons.exception.UpstreamServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String method = methodKey.substring(methodKey.indexOf('#') + 1, methodKey.indexOf('('));

        return switch (response.status()) {
            case 404 -> new EntityNotFoundException(notFoundMessage(method));
            default  -> new UpstreamServiceException("Product Service returned HTTP " + response.status());
        };
    }

    private String notFoundMessage(String method) {
        return switch (method) {
            case "getProductById" -> "Product not found";
            default -> "Resource not found on Product Service";
        };
    }
}
