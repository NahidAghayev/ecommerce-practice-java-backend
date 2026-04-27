package com.aghayev.ecommerce.dto.response;

public record ValidationErrorResponseDto(
        String fieldName,
        String message
) {
}
