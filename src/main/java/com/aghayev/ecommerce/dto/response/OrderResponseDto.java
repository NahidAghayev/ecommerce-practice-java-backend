package com.aghayev.ecommerce.dto.response;

import com.aghayev.ecommerce.entity.Order.Status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID id,
        UUID userId,
        Status status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderItemResponseDto> items
) {
}
