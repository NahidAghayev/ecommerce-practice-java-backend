package com.aghayev.ecommerce.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, data, message, LocalDateTime.now());
    }
}
