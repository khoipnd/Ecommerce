package com.fpt.ecommerce.service;

import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.repository.CategoryRepository;
import com.fpt.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // GET ALL
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // GET BY ID
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found with id: " + id
                        )
                );
    }

    // CREATE
    public Product create(Product product) {

        if (product.getCategory() == null
                || product.getCategory().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category id is required"
            );
        }

        Long categoryId = product.getCategory().getId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found with id: " + categoryId
                        )
                );

        product.setId(null);
        product.setCategory(category);

        return productRepository.save(product);
    }

    // UPDATE
    public Product update(Long id, Product product) {

        Product existingProduct = findById(id);

        if (product.getCategory() == null
                || product.getCategory().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category id is required"
            );
        }

        Long categoryId = product.getCategory().getId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found with id: " + categoryId
                        )
                );

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setCategory(category);

        return productRepository.save(existingProduct);
    }

    // DELETE
    public void delete(Long id) {

        Product product = findById(id);

        productRepository.delete(product);
    }
}
