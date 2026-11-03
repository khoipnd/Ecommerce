package com.fpt.productservice.controller;

import com.fpt.productservice.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {
    public record StockChange(@Min(1) int quantity) {}

    private final ProductRepository products;
    private final String apiKey;

    public InternalProductController(ProductRepository products, @Value("${internal.api-key}") String apiKey) {
        this.products = products;
        this.apiKey = apiKey;
    }

    @PostMapping("/{id}/stock/decrease")
    @Transactional
    public void decrease(@PathVariable Long id,
                         @RequestHeader(value = "X-Internal-Key", required = false) String key,
                         @Valid @RequestBody StockChange body) {
        checkKey(key);
        if (!products.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        if (products.decreaseStock(id, body.quantity()) == 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough stock");
    }

    @PostMapping("/{id}/stock/increase")
    @Transactional
    public void increase(@PathVariable Long id,
                         @RequestHeader(value = "X-Internal-Key", required = false) String key,
                         @Valid @RequestBody StockChange body) {
        checkKey(key);
        if (products.increaseStock(id, body.quantity()) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }

    // category-service gọi trước khi xoá category, để không để lại sản phẩm mồ côi
    @GetMapping("/count")
    public long countByCategory(@RequestParam Long categoryId,
                                @RequestHeader(value = "X-Internal-Key", required = false) String key) {
        checkKey(key);
        return products.countByCategoryId(categoryId);
    }

    private void checkKey(String key) {
        if (key == null || !key.equals(apiKey))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal API only");
    }
}
