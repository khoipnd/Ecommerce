package com.fpt.ecommerce.specification;

import com.fpt.ecommerce.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Các điều kiện lọc Product (BE-10). Mỗi hàm luôn trả về Specification không null;
 * nếu tham số null thì điều kiện đó không có tác dụng (cb.conjunction()).
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> matchAll() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null
                ? cb.conjunction()
                : cb.equal(root.get("category").<Long>get("id"), categoryId);
    }

    /** Tìm theo tên, không phân biệt hoa thường; ký tự % _ \ được escape. */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String escaped = keyword.trim().toLowerCase()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            return cb.like(cb.lower(root.<String>get("name")), "%" + escaped + "%", '\\');
        };
    }

    public static Specification<Product> priceAtLeast(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), minPrice);
    }

    public static Specification<Product> priceAtMost(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), maxPrice);
    }

    /** true: còn hàng (stock > 0); false: hết hàng (stock = 0); null: không lọc. */
    public static Specification<Product> inStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null) {
                return cb.conjunction();
            }
            return inStock
                    ? cb.greaterThan(root.<Integer>get("stockQuantity"), 0)
                    : cb.equal(root.<Integer>get("stockQuantity"), 0);
        };
    }
}
