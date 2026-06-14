package com.example.ecommerce.inventory.exception;

public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message) {
        super(message);
    }
}
