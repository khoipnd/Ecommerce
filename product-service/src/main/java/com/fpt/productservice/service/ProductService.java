package com.fpt.productservice.service;

import com.fpt.productservice.client.CategoryClient;
import com.fpt.productservice.entity.Product;
import com.fpt.productservice.exception.ResourceNotFoundException;
import com.fpt.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryClient categoryClient;

    public ProductService(
            ProductRepository productRepository,
            CategoryClient categoryClient
    ) {
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id = " + id
                        )
                );
    }

    public Product createProduct(Product product) {

        validateCategory(product.getCategoryId());

        product.setId(null);

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product newProduct) {

        Product product = getProductById(id);

        validateCategory(newProduct.getCategoryId());

        product.setName(newProduct.getName());
        product.setDescription(newProduct.getDescription());
        product.setPrice(newProduct.getPrice());
        product.setStockQuantity(newProduct.getStockQuantity());
        product.setCategoryId(newProduct.getCategoryId());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {

        Product product = getProductById(id);

        productRepository.delete(product);
    }

    private void validateCategory(Long categoryId) {

        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "Category ID must not be null"
            );
        }

        categoryClient.getCategoryById(categoryId);
    }
}