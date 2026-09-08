package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ErrorResponseDTO {
    private final String error;
    private final String message;
    private final Instant timestamp;

    public static ErrorResponseDTO of(String error, String message) {
        return ErrorResponseDTO.builder()
                .error(error)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}