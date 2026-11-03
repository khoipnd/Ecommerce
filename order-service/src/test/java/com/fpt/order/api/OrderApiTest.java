package com.fpt.order.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.order.client.ProductClient;
import com.fpt.order.dto.OrderItemRequest;
import com.fpt.order.dto.OrderRequest;
import com.fpt.order.dto.ProductInfo;
import com.fpt.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderApiTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private ProductClient productClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        orderRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "99", roles = "USER")
    @DisplayName("Luồng E2E: User đặt hàng và tự hủy đơn trên Database thật")
    void orderFlow() throws Exception {
        // Giả lập Product Client gọi sang Microservice Product
        when(productClient.getProduct(1L)).thenReturn(new ProductInfo(1L, "Test Product", new BigDecimal("500.0")));
        when(productClient.decreaseStock(1L, 2)).thenReturn(true);

        OrderRequest request = new OrderRequest("Alice", "0999999999", "Hanoi", "Fragile", List.of(new OrderItemRequest(1L, 2)));

        // 1. User đặt hàng (Lưu xuống DB)
        String res = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.totalAmount", is(1000.0)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(res).get("id").asLong();

        // 2. Hủy đơn (Cập nhật DB)
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}
