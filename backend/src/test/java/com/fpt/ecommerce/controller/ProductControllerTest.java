package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(100L);
        product1.setName("Laptop");
        product1.setPrice(new BigDecimal("999.99"));

        product2 = new Product();
        product2.setId(200L);
        product2.setName("Mouse");
        product2.setPrice(new BigDecimal("49.99"));
    }

    @Test
    void getAll_ShouldReturnProductList() {
        when(productService.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<Product> result = productController.getAll();

        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        verify(productService, times(1)).findAll();
    }

    @Test
    void getById_ShouldReturnProduct() {
        when(productService.findById(100L)).thenReturn(product1);

        Product result = productController.getById(100L);

        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("999.99"), result.getPrice());
        verify(productService, times(1)).findById(100L);
    }

    @Test
    void create_ShouldReturnCreatedProduct() {
        when(productService.create(any(Product.class))).thenReturn(product1);

        ResponseEntity<Product> response = productController.create(product1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Laptop", response.getBody().getName());
        verify(productService, times(1)).create(product1);
    }

    @Test
    void update_ShouldReturnUpdatedProduct() {
        when(productService.update(eq(100L), any(Product.class))).thenReturn(product1);

        Product result = productController.update(100L, product1);

        assertEquals("Laptop", result.getName());
        verify(productService, times(1)).update(100L, product1);
    }

    @Test
    void delete_ShouldCallServiceDelete() {
        doNothing().when(productService).delete(100L);

        productController.delete(100L);

        verify(productService, times(1)).delete(100L);
    }
}
