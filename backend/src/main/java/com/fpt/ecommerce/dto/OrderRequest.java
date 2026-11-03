package com.fpt.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequest(
        @NotBlank(message = "Receiver name must not be blank")
        @Size(max = 100, message = "Receiver name must be at most 100 characters") String receiverName,

        @NotBlank(message = "Receiver phone must not be blank")
        @Size(max = 20, message = "Receiver phone must be at most 20 characters") String receiverPhone,

        @NotBlank(message = "Shipping address must not be blank")
        @Size(max = 500, message = "Shipping address must be at most 500 characters") String shippingAddress,

        @Size(max = 500, message = "Note must be at most 500 characters") String note,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid List<OrderItemRequest> items) {
}