package com.fpt.order.service;

import com.fpt.order.client.ProductClient;
import com.fpt.order.dto.OrderItemRequest;
import com.fpt.order.dto.OrderRequest;
import com.fpt.order.dto.OrderResponse;
import com.fpt.order.dto.ProductInfo;
import com.fpt.order.entity.Order;
import com.fpt.order.entity.OrderItem;
import com.fpt.order.entity.OrderStatus;
import com.fpt.order.exception.ResourceNotFoundException;
import com.fpt.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // Các bước chuyển trạng thái hợp lệ
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, EnumSet.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));

    private record StockLine(Long productId, int quantity) {}

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    // ===== Khách hàng =====

    /**
     * Thêm đơn hàng / đặt hàng.
     * Không dùng @Transactional ở đây vì việc trừ kho nằm ở dịch vụ khác, DB của ta không rollback hộ được.
     * Thay vào đó, nếu có lỗi thì hoàn kho những sản phẩm đã trừ (bù trừ).
     */
    public OrderResponse create(Long accountId, OrderRequest req) {
        // Gộp các dòng trùng sản phẩm (order_item có UNIQUE (order_id, product_id)); TreeMap để xử lý theo thứ tự id
        Map<Long, Integer> quantities = new TreeMap<>();
        for (OrderItemRequest i : req.items()) {
            quantities.merge(i.productId(), i.quantity(), Integer::sum);
        }

        Order order = new Order();
        order.setAccountId(accountId);
        order.setReceiverName(req.receiverName().trim());
        order.setReceiverPhone(req.receiverPhone().trim());
        order.setShippingAddress(req.shippingAddress().trim());
        order.setNote(req.note() == null || req.note().isBlank() ? null : req.note().trim());

        List<StockLine> reserved = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        try {
            for (Map.Entry<Long, Integer> e : quantities.entrySet()) {
                Long productId = e.getKey();
                int qty = e.getValue();

                ProductInfo product = productClient.getProduct(productId);   // lấy tên, giá thật
                if (!productClient.decreaseStock(productId, qty)) {          // trừ kho bên Product
                    throw new IllegalArgumentException("Not enough stock for product: " + product.name());
                }
                reserved.add(new StockLine(productId, qty));

                OrderItem item = new OrderItem();
                item.setProductId(productId);
                item.setProductName(product.name());
                item.setQuantity(qty);
                item.setUnitPrice(product.price());
                order.addItem(item);

                total = total.add(product.price().multiply(BigDecimal.valueOf(qty)));
            }
            order.setTotalAmount(total);                                      // server tự tính tiền
            return OrderResponse.from(orderRepository.save(order));           // lưu đơn hàng vào DB
        } catch (RuntimeException ex) {
            giveBackStock(reserved);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByAccount(Long accountId) {
        return orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getForUser(Long id, Long accountId, boolean isAdmin) {
        return OrderResponse.from(findOwned(id, accountId, isAdmin));
    }

    @Transactional
    public OrderResponse cancelByOwner(Long id, Long accountId) {
        Order order = findOwned(id, accountId, false);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only a PENDING order can be cancelled");
        }
        changeStatus(order, OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    // ===== Admin =====

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(OrderStatus status) {
        List<Order> orders = (status == null)
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return OrderResponse.from(orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id = " + id)));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id = " + id));
        changeStatus(order, newStatus);
        return OrderResponse.from(order);
    }

    // ===== Nội bộ =====

    private Order findOwned(Long id, Long accountId, boolean isAdmin) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id = " + id));
        // Đơn của người khác trả 404 để không lộ việc đơn đó có tồn tại
        if (!isAdmin && !order.getAccountId().equals(accountId)) {
            throw new ResourceNotFoundException("Order not found with id = " + id);
        }
        return order;
    }

    private void changeStatus(Order order, OrderStatus next) {
        OrderStatus current = order.getStatus();
        if (!TRANSITIONS.get(current).contains(next)) {
            throw new IllegalArgumentException("Cannot change order status from " + current + " to " + next);
        }
        if (next == OrderStatus.CANCELLED) {
            // Hoàn kho bên Product trước; lỗi thì ném ra và không đổi trạng thái
            for (OrderItem item : order.getItems()) {
                productClient.increaseStock(item.getProductId(), item.getQuantity());
            }
        }
        order.setStatus(next);
    }

    private void giveBackStock(List<StockLine> lines) {
        for (StockLine l : lines) {
            try {
                productClient.increaseStock(l.productId(), l.quantity());
            } catch (RuntimeException e) {
                // Không che lỗi gốc; ghi log để xử lý tay
                log.error("Could not give back stock: product={}, qty={}", l.productId(), l.quantity(), e);
            }
        }
    }
}
