package com.fpt.order.dto;

import com.fpt.order.entity.Order;
import com.fpt.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long accountId,
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
                .map(i -> new Item(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        return new OrderResponse(o.getId(), o.getAccountId(), o.getReceiverName(), o.getReceiverPhone(),
                o.getShippingAddress(), o.getNote(), o.getStatus(), o.getTotalAmount(), o.getCreatedAt(), items);
    }
}
