package com.tcc.sspsp.dto;
 
import java.time.LocalDateTime;
 
public record ApiResponseDTO<T>(
    T data,
    String message,
    LocalDateTime timestamp,
    boolean success
) {
    public static <T> ApiResponseDTO<T> ok(T data) {
        return new ApiResponseDTO<>(data, "success", LocalDateTime.now(), true);
    }
 
    public static <T> ApiResponseDTO<T> error(String message) {
        return new ApiResponseDTO<>(null, message, LocalDateTime.now(), false);
    }
}