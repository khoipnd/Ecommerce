package com.fpt.ecommerce.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper trả về cho các API có phân trang (BE-11).
 * Dùng thay cho việc trả thẳng PageImpl để JSON ổn định, dễ dùng cho Frontend.
 * page là số trang bắt đầu từ 0.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
