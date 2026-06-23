package com.example.ecommerce.order.exception;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.commons.exception.UpstreamServiceException;
import feign.RetryableException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", message);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException exception) {
        return problemDetail(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ProblemDetail handleResourceConflictException(ResourceConflictException exception) {
        return problemDetail(HttpStatus.CONFLICT, "Resource conflict", exception.getMessage());
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ProblemDetail handleUpstreamServiceException(UpstreamServiceException exception) {
        return problemDetail(HttpStatus.SERVICE_UNAVAILABLE, "Upstream service error", exception.getMessage());
    }

    @ExceptionHandler(RetryableException.class)
    public ProblemDetail handleFeignRetryableException(RetryableException exception) {
        return problemDetail(HttpStatus.SERVICE_UNAVAILABLE, "Upstream service unreachable",
                "An upstream service is currently unavailable. Please try again later.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException exception) {
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", exception.getMessage());
    }

    private ProblemDetail problemDetail(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        return problemDetail;
    }
}
