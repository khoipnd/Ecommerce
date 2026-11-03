package com.fpt.order.dto;

import com.fpt.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull(message = "Status must not be null") OrderStatus status) {
}
