package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.dto.PageResponse;
import com.fpt.ecommerce.dto.ProductFilterCriteria;
import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.service.ProductFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductFilterControllerTest {

    @Mock
    private ProductFilterService productFilterService;

    @InjectMocks
    private ProductFilterController productFilterController;

    private PageResponse<Product> mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new PageResponse<>(
                Collections.emptyList(),
                0,
                10,
                0L,
                1,
                true,
                true
        );
    }

    @Test
    void filter_ShouldReturnPageResponse() {
        when(productFilterService.filter(any(ProductFilterCriteria.class))).thenReturn(mockResponse);

        PageResponse<Product> result = productFilterController.filter(
                1L, "test", new BigDecimal("10.0"), new BigDecimal("100.0"), true,
                0, 10, "id", "asc"
        );

        assertEquals(0L, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(productFilterService, times(1)).filter(any(ProductFilterCriteria.class));
    }
}
