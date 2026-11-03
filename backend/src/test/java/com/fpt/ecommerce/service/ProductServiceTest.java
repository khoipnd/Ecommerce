package com.fpt.ecommerce.service;

import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.repository.CategoryRepository;
import com.fpt.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product1 = new Product();
        product1.setId(100L);
        product1.setName("Laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setStockQuantity(10);
        product1.setCategory(category);

        product2 = new Product();
        product2.setId(200L);
        product2.setName("Mouse");
        product2.setPrice(new BigDecimal("49.99"));
        product2.setStockQuantity(50);
        product2.setCategory(category);
    }

    @Test
    void findAll_ShouldReturnProductList() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<Product> result = productService.findAll();

        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product1));

        Product result = productService.findById(100L);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        verify(productRepository, times(1)).findById(100L);
    }

    @Test
    void findById_WhenProductDoesNotExist_ShouldThrowResponseStatusException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.findById(999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void create_WhenValid_ShouldSaveAndReturnProduct() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        Product result = productService.create(product1);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product1);
    }

    @Test
    void create_WhenCategoryIsNull_ShouldThrowResponseStatusException() {
        Product invalidProduct = new Product();
        invalidProduct.setName("Invalid");
        // category is null

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.create(invalidProduct));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_WhenCategoryIdIsNull_ShouldThrowResponseStatusException() {
        Product invalidProduct = new Product();
        invalidProduct.setName("Invalid");
        invalidProduct.setCategory(new Category()); // id is null

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.create(invalidProduct));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_WhenCategoryNotFound_ShouldThrowResponseStatusException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.create(product1));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_WhenValid_ShouldUpdateAndReturnProduct() {
        Product input = new Product();
        input.setName("Updated Laptop");
        input.setPrice(new BigDecimal("1099.99"));
        input.setStockQuantity(5);
        input.setCategory(category);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product1));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        Product result = productService.update(100L, input);

        assertEquals("Updated Laptop", result.getName());
        assertEquals(new BigDecimal("1099.99"), result.getPrice());
        verify(productRepository, times(1)).findById(100L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product1);
    }

    @Test
    void update_WhenCategoryIsNull_ShouldThrowResponseStatusException() {
        Product input = new Product();
        input.setName("Updated Laptop");

        when(productRepository.findById(100L)).thenReturn(Optional.of(product1));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.update(100L, input));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_WhenCategoryNotFound_ShouldThrowResponseStatusException() {
        Product input = new Product();
        input.setName("Updated Laptop");
        input.setCategory(category);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product1));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> productService.update(100L, input));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_WhenProductExists_ShouldDeleteProduct() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product1));
        doNothing().when(productRepository).delete(product1);

        productService.delete(100L);

        verify(productRepository, times(1)).findById(100L);
        verify(productRepository, times(1)).delete(product1);
    }
}
