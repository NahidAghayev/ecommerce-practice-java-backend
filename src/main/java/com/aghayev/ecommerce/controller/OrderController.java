package com.aghayev.ecommerce.controller;

import com.aghayev.ecommerce.dto.ApiResponse;
import com.aghayev.ecommerce.dto.PageResponse;
import com.aghayev.ecommerce.dto.request.OrderRequestDto;
import com.aghayev.ecommerce.dto.request.OrderStatusUpdateRequestDto;
import com.aghayev.ecommerce.dto.response.OrderResponseDto;
import com.aghayev.ecommerce.service.OrderService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(@Valid @RequestBody OrderRequestDto requestDto) {
        OrderResponseDto createdOrder = orderService.placeOrder(requestDto);
        return ResponseEntity
                .created(URI.create("/api/orders/" + createdOrder.id()))
                .body(ApiResponse.success(createdOrder, "Order placed successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id), "Order retrieved successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateStatusByOrderId(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequestDto requestDto
    ) {
        OrderResponseDto orderResponseDto = orderService.updateOrderStatus(id, requestDto);

        return ResponseEntity.ok(ApiResponse.success(orderResponseDto, "Order status changed successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getMyOrders(
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(pageable), "Orders retrieved successfully"));
    }
}
