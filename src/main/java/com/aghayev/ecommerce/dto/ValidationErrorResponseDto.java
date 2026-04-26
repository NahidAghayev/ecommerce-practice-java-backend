package com.aghayev.ecommerce.dto;

public record ValidationErrorResponseDto(
        String fieldName,
        String message
) {
}
