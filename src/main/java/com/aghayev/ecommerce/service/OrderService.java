package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.config.LogExecutionTime;
import com.aghayev.ecommerce.dto.PageResponse;
import com.aghayev.ecommerce.dto.request.OrderItemRequestDto;
import com.aghayev.ecommerce.dto.request.OrderRequestDto;
import com.aghayev.ecommerce.dto.request.OrderStatusUpdateRequestDto;
import com.aghayev.ecommerce.dto.response.OrderResponseDto;
import com.aghayev.ecommerce.entity.Order;
import com.aghayev.ecommerce.entity.OrderItem;
import com.aghayev.ecommerce.entity.Product;
import com.aghayev.ecommerce.entity.User;
import com.aghayev.ecommerce.exception.BadRequestException;
import com.aghayev.ecommerce.exception.ForbiddenException;
import com.aghayev.ecommerce.exception.InsufficientStockException;
import com.aghayev.ecommerce.exception.ResourceNotFoundException;
import com.aghayev.ecommerce.mapper.OrderMapper;
import com.aghayev.ecommerce.repository.OrderRepository;
import com.aghayev.ecommerce.repository.ProductRepository;
import com.aghayev.ecommerce.repository.UserRepository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    private final OrderMapper orderMapper;

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
        return orderMapper.toResponseDto(savedOrder);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID id) {

        User currentUser = getCurrentAuthenticatedUser();

        log.debug("action=getOrderById orderId={} requesterUserId={}", id, currentUser.getId());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (!canAccessOrder(currentUser, order)) {
            log.warn(
                    "action=getOrderById status=ACCESS_DENIED orderId={} requesterUserId={} ownerUserId={}",
                    id,
                    currentUser.getId(),
                    order.getUser().getId()
            );

            throw new ForbiddenException("You do not have permission to access this order", "orderId");
        }

        return orderMapper.toResponseDto(order);
    }

    @LogExecutionTime
    @Transactional(readOnly = true)
    public PageResponse<OrderResponseDto> getMyOrders(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        log.debug("action=getMyOrders userId={}", currentUser.getId());
        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);

        Page<OrderResponseDto> mappedOrders = orders.map(orderMapper::toResponseDto);

        return PageResponse.from(mappedOrders);
    }

    @LogExecutionTime
    @Transactional
    public OrderResponseDto updateOrderStatus(UUID id, OrderStatusUpdateRequestDto requestDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        Order.Status currentStatus = order.getStatus();
        Order.Status newStatus = requestDto.status();
        log.info(
                "action=updateOrderStatus orderId={} currentStatus={} newStatus={}",
                id,
                currentStatus,
                newStatus
        );
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            log.warn(
                    "action=updateOrderStatus status=INVALID_TRANSITION orderId={} currentStatus={} newStatus={}",
                    id,
                    currentStatus,
                    newStatus
            );

            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus, "status");
        }

        if (newStatus == Order.Status.CANCELLED) {
            log.info(
                    "action=updateOrderStatus status=RESTORE_STOCK orderId={} currentStatus={} newStatus={}",
                    id,
                    currentStatus,
                    newStatus
            );

            restoreStock(order);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info(
                "action=updateOrderStatus status=SUCCESS orderId={} previousStatus={} newStatus={}",
                updatedOrder.getId(),
                currentStatus,
                newStatus
        );

        return orderMapper.toResponseDto(updatedOrder);
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

    private boolean isValidStatusTransition(Order.Status currentStatus, Order.Status newStatus) {
        return (currentStatus == Order.Status.PENDING
                && (newStatus == Order.Status.CONFIRMED || newStatus == Order.Status.CANCELLED))
                || (currentStatus == Order.Status.CONFIRMED
                && (newStatus == Order.Status.SHIPPED || newStatus == Order.Status.CANCELLED))
                || (currentStatus == Order.Status.SHIPPED
                && newStatus == Order.Status.DELIVERED);
    }

    private void restoreStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
        }
    }

    private boolean canAccessOrder(User currentUser, Order order) {
        return currentUser.getRole() == User.Role.ADMIN
                || order.getUser().getId().equals(currentUser.getId());
    }
}
