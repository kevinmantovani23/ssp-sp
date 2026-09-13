package com.tcc.sspsp.dto;

import java.time.LocalDateTime;

public record ApiResponseDTO<T>(
    T data,
    String codigo,
    String message,
    LocalDateTime timestamp,
    boolean success
) {
    public static <T> ApiResponseDTO<T> ok(T data) {
        return new ApiResponseDTO<>(data, null, "success", LocalDateTime.now(), true);
    }

    public static <T> ApiResponseDTO<T> error(String codigo, String message) {
        return new ApiResponseDTO<>(null, codigo, message, LocalDateTime.now(), false);
    }
}
