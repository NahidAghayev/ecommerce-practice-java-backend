package com.aghayev.ecommerce.repository;

import com.aghayev.ecommerce.entity.Order;
import com.aghayev.ecommerce.entity.User;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    Optional<Order> findById(UUID id);
}
