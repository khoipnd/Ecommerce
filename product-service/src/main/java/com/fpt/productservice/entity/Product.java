package com.fpt.productservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name must not be blank")
    @Size(max = 255, message = "Product name must be at most 255 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Price must not be null")
    @Positive(message = "Price must be greater than 0")
    @Digits(integer = 16, fraction = 2, message = "Price must have at most 16 integer digits and 2 decimals")
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity must not be null")
    @PositiveOrZero(message = "Stock quantity must not be negative")
    @Column(nullable = false, name = "stock_quantity")
    private Integer stockQuantity;

    @NotNull(message = "Category id must not be null")
    @Column(nullable = false, name = "category_id")
    private Long categoryId;
}