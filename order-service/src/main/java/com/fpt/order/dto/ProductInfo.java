package com.fpt.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Dữ liệu nhận từ product-service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductInfo(Long id, String name, BigDecimal price) {
}
