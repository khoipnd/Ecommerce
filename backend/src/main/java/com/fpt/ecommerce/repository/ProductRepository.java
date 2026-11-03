package com.fpt.ecommerce.repository;

import com.fpt.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Chỉ trừ khi còn đủ hàng, trả về 0 nếu không đủ
    @Modifying
    @Query("update Product p set p.stockQuantity = p.stockQuantity - :qty " +
            "where p.id = :id and p.stockQuantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    // Dùng khi hủy đơn để hoàn lại kho
    @Modifying
    @Query("update Product p set p.stockQuantity = p.stockQuantity + :qty where p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);
}