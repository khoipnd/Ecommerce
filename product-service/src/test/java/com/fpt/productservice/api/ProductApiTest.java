package com.fpt.productservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.productservice.client.CategoryClient;
import com.fpt.productservice.client.CategoryResponse;
import com.fpt.productservice.entity.Product;
import com.fpt.productservice.repository.ProductRepository;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class ProductApiTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private CategoryClient categoryClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        productRepository.deleteAll(); // Clean DB before each test
    }

    @Test
    @DisplayName("Luồng API E2E: Tạo, Đọc, Sửa, Xóa")
    void fullFlow() throws Exception {
        // Mock CategoryClient trả về hợp lệ (để pass logic validate)
        CategoryResponse catRes = new CategoryResponse();
        when(categoryClient.getCategoryById(100L)).thenReturn(catRes);

        // 1. Tạo Product
        Product newProduct = new Product();
        newProduct.setName("Phone");
        newProduct.setPrice(new BigDecimal("999.99"));
        newProduct.setStockQuantity(10);
        newProduct.setCategoryId(100L);

        String responseBody = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        Product savedProduct = objectMapper.readValue(responseBody, Product.class);
        Long id = savedProduct.getId();

        // 2. Đọc Product (Get All & Get By Id)
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Phone")));

        // 3. Sửa Product
        Product updateInfo = new Product();
        updateInfo.setName("SmartPhone");
        updateInfo.setPrice(new BigDecimal("1099.99"));
        updateInfo.setStockQuantity(20);
        updateInfo.setCategoryId(100L);

        mockMvc.perform(put("/api/v1/products/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("SmartPhone")));

        // 4. Xóa Product
        mockMvc.perform(delete("/api/v1/products/" + id))
                .andExpect(status().isNoContent());

        // Verify xóa thành công
        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isNotFound());
        assertFalse(productRepository.existsById(id));
    }
}
