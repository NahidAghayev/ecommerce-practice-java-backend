package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.config.LogExecutionTime;
import com.aghayev.ecommerce.dto.OrderItemRequestDto;
import com.aghayev.ecommerce.dto.OrderItemResponseDto;
import com.aghayev.ecommerce.dto.OrderRequestDto;
import com.aghayev.ecommerce.dto.OrderResponseDto;
import com.aghayev.ecommerce.entity.Order;
import com.aghayev.ecommerce.entity.OrderItem;
import com.aghayev.ecommerce.entity.Product;
import com.aghayev.ecommerce.entity.User;
import com.aghayev.ecommerce.exception.BadRequestException;
import com.aghayev.ecommerce.exception.InsufficientStockException;
import com.aghayev.ecommerce.exception.ResourceNotFoundException;
import com.aghayev.ecommerce.repository.OrderRepository;
import com.aghayev.ecommerce.repository.ProductRepository;
import com.aghayev.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @LogExecutionTime
    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto requestDto) {
        User currentUser = getCurrentAuthenticatedUser();
        log.info(
                "action=placeOrder userId={} itemCount={}",
                currentUser.getId(),
                requestDto.items().size()
        );
        Order order = Order.builder()
                .user(currentUser)
                .status(Order.Status.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDto itemRequest : requestDto.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.productId()
                    ));

            log.debug(
                    "action=placeOrder productId={} requestedQuantity={} availableStock={}",
                    product.getId(),
                    itemRequest.quantity(),
                    product.getStockQuantity()
            );

            if (product.getStockQuantity() < itemRequest.quantity()) {
                log.warn(
                        "action=placeOrder status=INSUFFICIENT_STOCK productId={} requestedQuantity={} availableStock={}",
                        product.getId(),
                        itemRequest.quantity(),
                        product.getStockQuantity()
                );
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName(),
                        "stockQuantity"
                );
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info(
                "action=placeOrder status=SUCCESS orderId={} userId={} totalAmount={}",
                savedOrder.getId(),
                currentUser.getId(),
                savedOrder.getTotalAmount()
        );
        return toResponseDto(savedOrder);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID id) {
        log.debug("action=getOrderById orderId={}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return toResponseDto(order);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders() {
        User currentUser = getCurrentAuthenticatedUser();
        log.debug("action=getMyOrders userId={}", currentUser.getId());
        return orderRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            log.warn("action=getCurrentAuthenticatedUser status=UNAUTHENTICATED");
            throw new BadRequestException("User is not authenticated");
        }

        log.debug("action=getCurrentAuthenticatedUser email={}", authentication.getName());
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found with email: " + authentication.getName()
                ));
    }

    private OrderResponseDto toResponseDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getOrderItems().stream()
                        .map(orderItem -> new OrderItemResponseDto(
                                orderItem.getProduct().getId(),
                                orderItem.getProduct().getName(),
                                orderItem.getQuantity(),
                                orderItem.getUnitPrice()
                        ))
                        .toList()
        );
    }
}
