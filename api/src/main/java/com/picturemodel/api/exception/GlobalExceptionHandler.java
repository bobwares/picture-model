/**
 * App: Picture Model
 * Package: com.picturemodel.api.exception
 * File: GlobalExceptionHandler.java
 * Version: 0.1.2
 * Turns: 5,17
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-03T05:20:19Z
 * Exports: GlobalExceptionHandler
 * Description: class GlobalExceptionHandler for GlobalExceptionHandler responsibilities. Methods: handleValidationException - handle validation exception; handleIllegalArgumentException - handle illegal argument exception; handleRuntimeException - handle runtime exception; handleGenericException - handle generic exception; formatFieldError - format field error.
 */

package com.picturemodel.api.exception;

import com.picturemodel.api.dto.response.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers (WebFlux).
 * Converts exceptions to standardized ErrorDto responses wrapped in Mono.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle servlet async request timeouts (Spring MVC adapts reactive return values via async).
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public Mono<ResponseEntity<ErrorDto>> handleAsyncRequestTimeout(AsyncRequestTimeoutException ex) {
        log.warn("Async request timeout");
        ErrorDto error = ErrorDto.of("timeout", "Request timed out");
        return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error));
    }

    /**
     * Handle validation errors (WebFlux).
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorDto>> handleValidationException(WebExchangeBindException ex) {
        log.error("Validation error", ex);

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        ErrorDto error = ErrorDto.of("bad_request", "Validation failed", details);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    /**
     * Handle illegal argument exceptions (e.g., entity not found).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDto>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument", ex);

        ErrorDto error = ErrorDto.of("not_found", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    /**
     * Handle runtime exceptions (e.g., connection errors).
     */
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorDto>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error", ex);

        ErrorDto error = ErrorDto.of("server_error", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorDto>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorDto error = ErrorDto.of("server_error", "An unexpected error occurred", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    /**
     * Format a field error into a readable string.
     */
    private String formatFieldError(FieldError error) {
        return String.format("%s: %s", error.getField(), error.getDefaultMessage());
    }
}
