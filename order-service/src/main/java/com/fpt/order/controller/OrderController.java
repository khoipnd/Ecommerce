package com.fpt.order.controller;

import com.fpt.order.dto.OrderRequest;
import com.fpt.order.dto.OrderResponse;
import com.fpt.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(accountId(auth), request));
    }

    @GetMapping({"", "/my"})
    public List<OrderResponse> myOrders(Authentication auth) {
        return orderService.findByAccount(accountId(auth));
    }

    @GetMapping("/{id}")
    public OrderResponse getOne(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return orderService.getForUser(id, accountId(auth), isAdmin);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id, Authentication auth) {
        return orderService.cancelByOwner(id, accountId(auth));
    }

    private Long accountId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}
