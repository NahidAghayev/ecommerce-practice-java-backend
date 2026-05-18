package com.aghayev.ecommerce.dto.request;

import com.aghayev.ecommerce.entity.Order;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequestDto(
        @NotNull(message = "Status is required")
        Order.Status status
) {
}
