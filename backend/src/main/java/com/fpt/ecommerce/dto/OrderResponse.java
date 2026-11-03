package com.fpt.ecommerce.dto;

import com.fpt.ecommerce.entity.Order;
import com.fpt.ecommerce.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String username,
        String receiverName,
        String receiverPhone,
        String shippingAddress,
        String note,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<Item> items) {

    public record Item(Long productId, String productName, Integer quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public static OrderResponse from(Order o) {
        List<Item> items = o.getItems().stream()
                .map(i -> new Item(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();

        return new OrderResponse(o.getId(), o.getAccount().getUsername(),
                o.getReceiverName(), o.getReceiverPhone(), o.getShippingAddress(), o.getNote(),
                o.getStatus(), o.getTotalAmount(), o.getCreatedAt(), items);
    }
}