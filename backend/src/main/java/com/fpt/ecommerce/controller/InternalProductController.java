package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.exception.ResourceNotFoundException;
import com.fpt.ecommerce.repository.ProductRepository;
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

    private final ProductRepository productRepository;
    private final String apiKey;

    public InternalProductController(ProductRepository productRepository,
                                     @Value("${internal.api-key:default-internal-secret-key-123456}") String apiKey) {
        this.productRepository = productRepository;
        this.apiKey = apiKey;
    }

    @PostMapping("/{id}/stock/decrease")
    @Transactional
    public void decrease(@PathVariable Long id,
                         @RequestHeader(value = "X-Internal-Key", required = false) String key,
                         @Valid @RequestBody StockChange body) {
        check(key);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id = " + id);
        }
        // Chỉ trừ khi đủ hàng; trả 409 nếu không đủ
        if (productRepository.decreaseStock(id, body.quantity()) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough stock");
        }
    }

    @PostMapping("/{id}/stock/increase")
    @Transactional
    public void increase(@PathVariable Long id,
                         @RequestHeader(value = "X-Internal-Key", required = false) String key,
                         @Valid @RequestBody StockChange body) {
        check(key);
        if (productRepository.increaseStock(id, body.quantity()) == 0) {
            throw new ResourceNotFoundException("Product not found with id = " + id);
        }
    }

    private void check(String key) {
        if (key == null || !key.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal API only");
        }
    }
}