package com.fpt.order.service;

import com.fpt.order.client.ProductClient;
import com.fpt.order.dto.OrderItemRequest;
import com.fpt.order.dto.OrderRequest;
import com.fpt.order.dto.OrderResponse;
import com.fpt.order.dto.ProductInfo;
import com.fpt.order.entity.Order;
import com.fpt.order.entity.OrderItem;
import com.fpt.order.entity.OrderStatus;
import com.fpt.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest request;

    @BeforeEach
    void setUp() {
        request = new OrderRequest(
                "John Doe", "0123456789", "123 Main St", "Note",
                List.of(new OrderItemRequest(1L, 2))
        );
    }

    @Test
    @DisplayName("Tạo đơn hàng thành công")
    void createOrderSuccess() {
        when(productClient.getProduct(1L)).thenReturn(new ProductInfo(1L, "Laptop", new BigDecimal("1000")));
        when(productClient.decreaseStock(1L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponse res = orderService.create(1L, request);

        assertEquals("John Doe", res.receiverName());
        assertEquals(new BigDecimal("2000"), res.totalAmount());
        verify(productClient).decreaseStock(1L, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Tạo đơn hàng thất bại khi không đủ kho, kiểm tra không lưu DB")
    void createOrderNotEnoughStock() {
        when(productClient.getProduct(1L)).thenReturn(new ProductInfo(1L, "Laptop", new BigDecimal("1000")));
        when(productClient.decreaseStock(1L, 2)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> orderService.create(1L, request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Hủy đơn hàng thành công")
    void cancelOrderSuccess() {
        Order order = new Order();
        order.setId(10L);
        order.setAccountId(1L);
        order.setStatus(OrderStatus.PENDING);
        
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100"));
        order.addItem(item);

        when(orderRepository.findWithDetailsById(10L)).thenReturn(Optional.of(order));

        OrderResponse res = orderService.cancelByOwner(10L, 1L);

        assertEquals(OrderStatus.CANCELLED, res.status());
        verify(productClient).increaseStock(1L, 2); // Trả lại kho
    }
}
