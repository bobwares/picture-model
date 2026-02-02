/**
 * App: Picture Model
 * Package: com.picturemodel.api.exception
 * File: GlobalExceptionHandler.java
 * Version: 0.1.1
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-31T02:19:37Z
 * Exports: GlobalExceptionHandler
 * Description: class GlobalExceptionHandler for GlobalExceptionHandler responsibilities. Methods: handleValidationException - handle validation exception; handleIllegalArgumentException - handle illegal argument exception; handleRuntimeException - handle runtime exception; handleGenericException - handle generic exception; formatFieldError - format field error.
 */

package com.picturemodel.api.exception;

import com.picturemodel.api.dto.response.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Converts exceptions to standardized ErrorDto responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error", ex);

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        ErrorDto error = ErrorDto.of("bad_request", "Validation failed", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle illegal argument exceptions (e.g., entity not found).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument", ex);

        ErrorDto error = ErrorDto.of("not_found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle runtime exceptions (e.g., connection errors).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error", ex);

        ErrorDto error = ErrorDto.of("server_error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorDto error = ErrorDto.of("server_error", "An unexpected error occurred", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle missing static or unmapped resources.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getResourcePath());

        ErrorDto error = ErrorDto.of("not_found", "Resource not found", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Format a field error into a readable string.
     */
    private String formatFieldError(FieldError error) {
        return String.format("%s: %s", error.getField(), error.getDefaultMessage());
    }
}
