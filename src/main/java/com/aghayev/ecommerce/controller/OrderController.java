package com.aghayev.ecommerce.controller;

import com.aghayev.ecommerce.dto.ApiResponse;
import com.aghayev.ecommerce.dto.request.OrderRequestDto;
import com.aghayev.ecommerce.dto.response.OrderResponseDto;
import com.aghayev.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(), "Orders retrieved successfully"));
    }
}
