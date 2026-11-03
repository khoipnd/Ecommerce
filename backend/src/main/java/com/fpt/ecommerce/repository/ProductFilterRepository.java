package com.fpt.ecommerce.repository;

import com.fpt.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

/**
 * Repository riêng cho lọc + phân trang Product (BE-10, BE-11).
 * Tách riêng để không phải sửa ProductRepository đã có.
 */
public interface ProductFilterRepository
        extends Repository<Product, Long>, JpaSpecificationExecutor<Product> {
}
