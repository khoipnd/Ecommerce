package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.dto.PageResponse;
import com.fpt.ecommerce.dto.ProductFilterCriteria;
import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.service.ProductFilterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * BE-10 Filter Products + BE-11 Pagination.
 *
 * GET /api/v1/products/filter
 *   categoryId, keyword, minPrice, maxPrice, inStock   (tất cả tùy chọn)
 *   page (mặc định 0), size (mặc định 10, tối đa 100)
 *   sortBy (id|name|price|stockQuantity, mặc định id), direction (asc|desc, mặc định asc)
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductFilterController {

    private final ProductFilterService productFilterService;

    public ProductFilterController(ProductFilterService productFilterService) {
        this.productFilterService = productFilterService;
    }

    @GetMapping("/filter")
    public PageResponse<Product> filter(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "inStock", required = false) Boolean inStock,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction) {

        return productFilterService.filter(new ProductFilterCriteria(
                categoryId, keyword, minPrice, maxPrice, inStock,
                page, size, sortBy, direction));
    }
}
