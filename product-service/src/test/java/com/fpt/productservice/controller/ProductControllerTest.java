package com.fpt.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.productservice.entity.Product;
import com.fpt.productservice.exception.ResourceNotFoundException;
import com.fpt.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ProductControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("GET /api/v1/products - Lấy danh sách sản phẩm")
    void getAll() throws Exception {
        Product p = new Product(); p.setId(1L); p.setName("P1");
        when(productService.getAllProducts()).thenReturn(Arrays.asList(p));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("P1"));
    }

    @Test
    @DisplayName("POST /api/v1/products - Tạo mới sản phẩm")
    void create() throws Exception {
        Product p = new Product(); p.setName("P1"); p.setPrice(new BigDecimal("1000")); p.setCategoryId(10L); p.setStockQuantity(10);
        Product saved = new Product(); saved.setId(1L); saved.setName("P1"); saved.setPrice(new BigDecimal("1000")); saved.setCategoryId(10L); saved.setStockQuantity(10);

        when(productService.createProduct(any(Product.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/v1/products - Dữ liệu sai trả 400 theo từng field, không gọi service")
    void createInvalid() throws Exception {
        String body = "{\"name\":\" \",\"price\":-5,\"stockQuantity\":-1}";

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.stockQuantity").exists())
                .andExpect(jsonPath("$.categoryId").exists());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Thiếu giá trả 400, không gọi service")
    void updateInvalid() throws Exception {
        String body = "{\"name\":\"P1\",\"stockQuantity\":1,\"categoryId\":10}";

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.price").exists());

        verify(productService, never()).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Xóa sản phẩm")
    void deleteProd() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
        
        verify(productService).deleteProduct(1L);
    }
}
