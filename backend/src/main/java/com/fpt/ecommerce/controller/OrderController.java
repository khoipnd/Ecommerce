package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.dto.OrderRequest;
import com.fpt.ecommerce.dto.OrderResponse;
import com.fpt.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request,
                                                Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(username(auth), request));
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(Authentication auth) {
        return orderService.findByUsername(username(auth));
    }

    @GetMapping("/{id}")
    public OrderResponse getOne(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return orderService.getForUser(id, username(auth), isAdmin);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id, Authentication auth) {
        return orderService.cancelByOwner(id, username(auth));
    }

    private String username(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Login required");
        }
        return auth.getName();
    }
}