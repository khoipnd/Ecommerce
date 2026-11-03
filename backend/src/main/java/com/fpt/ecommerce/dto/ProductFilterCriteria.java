package com.fpt.ecommerce.dto;

import java.math.BigDecimal;

/**
 * Các điều kiện lọc + phân trang + sắp xếp cho danh sách sản phẩm (BE-10, BE-11).
 * Các trường lọc có thể null (null = không lọc theo trường đó).
 */
public record ProductFilterCriteria(
        Long categoryId,
        String keyword,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        int page,
        int size,
        String sortBy,
        String direction
) {
}
