package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET ALL
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Product> create(
            @Valid @RequestBody Product product) {

        Product createdProduct = productService.create(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Product update(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {

        return productService.update(id, product);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        productService.delete(id);
    }
}
