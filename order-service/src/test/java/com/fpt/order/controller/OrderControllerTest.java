package com.fpt.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.order.dto.OrderRequest;
import com.fpt.order.dto.OrderResponse;
import com.fpt.order.entity.OrderStatus;
import com.fpt.order.service.OrderService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("POST /api/v1/orders - Đặt hàng thành công với tài khoản đã Login")
    void createOrder() throws Exception {
        OrderRequest req = new OrderRequest("Receiver", "0123456789", "Address", "", List.of(new com.fpt.order.dto.OrderItemRequest(1L, 1)));
        OrderResponse res = new OrderResponse(10L, 1L, "Receiver", "0123456789", "Address", "", OrderStatus.PENDING, new BigDecimal("100"), LocalDateTime.now(), List.of());
        
        when(orderService.create(eq(1L), any(OrderRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Bị 403 Forbidden do chưa Login (Không có token)")
    void createOrderUnauthorized() throws Exception {
        OrderRequest req = new OrderRequest("Receiver", "0123456789", "Address", "", List.of());
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
