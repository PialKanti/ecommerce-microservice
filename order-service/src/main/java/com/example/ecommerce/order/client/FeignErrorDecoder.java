package com.example.ecommerce.order.client;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.commons.exception.UpstreamServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String upstream = resolveUpstream(methodKey);
        String method = methodKey.substring(methodKey.indexOf('#') + 1, methodKey.indexOf('('));

        return switch (response.status()) {
            case 404 -> new EntityNotFoundException(notFoundMessage(method));
            case 409 -> new ResourceConflictException(conflictMessage(method));
            default  -> new UpstreamServiceException(upstream + " returned HTTP " + response.status());
        };
    }

    private String resolveUpstream(String methodKey) {
        if (methodKey.contains("CartServiceClient"))      return "Cart Service";
        if (methodKey.contains("InventoryServiceClient")) return "Inventory Service";
        return "Upstream Service";
    }

    private String notFoundMessage(String method) {
        return switch (method) {
            case "getCart"  -> "Cart not found for user";
            case "reserve", "release" -> "Inventory not found for product";
            default -> "Resource not found on upstream service";
        };
    }

    private String conflictMessage(String method) {
        return switch (method) {
            case "reserve" -> "Insufficient available inventory for one or more products";
            case "release" -> "Cannot release more inventory than currently reserved";
            default -> "Resource conflict on upstream service";
        };
    }
}
