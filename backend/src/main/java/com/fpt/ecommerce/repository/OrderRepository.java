package com.fpt.ecommerce.repository;

import com.fpt.ecommerce.entity.Order;
import com.fpt.ecommerce.entity.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"account", "items", "items.product"})
    Optional<Order> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"account", "items", "items.product"})
    List<Order> findByAccountUsernameOrderByCreatedAtDesc(String username);

    @EntityGraph(attributePaths = {"account", "items", "items.product"})
    List<Order> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"account", "items", "items.product"})
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}