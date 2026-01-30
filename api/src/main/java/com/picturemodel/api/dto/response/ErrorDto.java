/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.response
 * File: ErrorDto.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: ErrorDto
 * Description: class ErrorDto for ErrorDto responsibilities. Methods: of - of; of - of.
 */

package com.picturemodel.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDto {

    private String errorCode;

    private String message;

    private String details;

    private LocalDateTime timestamp;

    public static ErrorDto of(String errorCode, String message) {
        return ErrorDto.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorDto of(String errorCode, String message, String details) {
        return ErrorDto.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
