package com.aghayev.ecommerce.mapper;

import com.aghayev.ecommerce.dto.response.OrderItemResponseDto;
import com.aghayev.ecommerce.dto.response.OrderResponseDto;
import com.aghayev.ecommerce.entity.Order;
import com.aghayev.ecommerce.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponseDto toResponseDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getOrderItems().stream()
                        .map(this::toOrderItemResponseDto)
                        .toList()
        );
    }

    public OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem) {
        return new OrderItemResponseDto(
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice()
        );
    }
}
