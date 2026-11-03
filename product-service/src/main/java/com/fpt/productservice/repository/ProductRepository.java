package com.fpt.productservice.repository;

import com.fpt.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    long countByCategoryId(Long categoryId);

    @Modifying
    @Query("update Product p set p.stockQuantity = p.stockQuantity - :qty where p.id = :id and p.stockQuantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("update Product p set p.stockQuantity = p.stockQuantity + :qty where p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);
}
