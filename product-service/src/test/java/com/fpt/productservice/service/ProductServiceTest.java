package com.fpt.productservice.service;

import com.fpt.productservice.client.CategoryClient;
import com.fpt.productservice.client.CategoryResponse;
import com.fpt.productservice.entity.Product;
import com.fpt.productservice.exception.ResourceNotFoundException;
import com.fpt.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryClient categoryClient;

    @InjectMocks
    private ProductService productService;

    private Product product1;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("10.0"));
        product1.setCategoryId(100L);
    }

    @Test
    @DisplayName("Lấy danh sách tất cả sản phẩm")
    void getAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1));
        List<Product> products = productService.getAllProducts();
        assertEquals(1, products.size());
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID thành công")
    void getProductByIdSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        Product found = productService.getProductById(1L);
        assertEquals("Product 1", found.getName());
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID thất bại do không tồn tại")
    void getProductByIdNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    @DisplayName("Tạo sản phẩm thành công")
    void createProductSuccess() {
        CategoryResponse catRes = new CategoryResponse();
        when(categoryClient.getCategoryById(100L)).thenReturn(catRes);
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        Product result = productService.createProduct(product1);
        assertEquals("Product 1", result.getName());
        verify(categoryClient).getCategoryById(100L);
        verify(productRepository).save(product1);
    }

    @Test
    @DisplayName("Tạo sản phẩm thất bại do categoryId null")
    void createProductNullCategory() {
        product1.setCategoryId(null);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product1));
    }

    @Test
    @DisplayName("Tạo sản phẩm thất bại do category không tồn tại")
    void createProductInvalidCategory() {
        when(categoryClient.getCategoryById(100L)).thenThrow(new ResourceNotFoundException("Not found"));
        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(product1));
    }

    @Test
    @DisplayName("Cập nhật sản phẩm thành công")
    void updateProductSuccess() {
        Product updateData = new Product();
        updateData.setName("Updated");
        updateData.setCategoryId(100L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(categoryClient.getCategoryById(100L)).thenReturn(new CategoryResponse());
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        Product result = productService.updateProduct(1L, updateData);
        assertEquals("Updated", product1.getName());
        verify(productRepository).save(product1);
    }

    @Test
    @DisplayName("Xóa sản phẩm thành công")
    void deleteProductSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        productService.deleteProduct(1L);
        verify(productRepository).delete(product1);
    }
}
