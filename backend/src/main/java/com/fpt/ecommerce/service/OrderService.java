package com.fpt.ecommerce.service;

import com.fpt.ecommerce.dto.OrderItemRequest;
import com.fpt.ecommerce.dto.OrderRequest;
import com.fpt.ecommerce.dto.OrderResponse;
import com.fpt.ecommerce.entity.*;
import com.fpt.ecommerce.exception.ResourceNotFoundException;
import com.fpt.ecommerce.repository.AccountRepository;
import com.fpt.ecommerce.repository.OrderRepository;
import com.fpt.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
@Transactional(readOnly = true)
public class    OrderService {

    // Các bước chuyển trạng thái hợp lệ
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, EnumSet.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        AccountRepository accountRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
    }

    // ===== Khách hàng =====

    @Transactional
    public OrderResponse create(String username, OrderRequest req) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + username));

        // Gộp các dòng trùng sản phẩm (bảng order_item có UNIQUE (order_id, product_id)).
        // TreeMap giúp luôn trừ kho theo thứ tự id, giảm nguy cơ deadlock khi nhiều đơn đặt cùng lúc.
        Map<Long, Integer> quantities = new TreeMap<>();
        for (OrderItemRequest i : req.items()) {
            quantities.merge(i.productId(), i.quantity(), Integer::sum);
        }

        Order order = new Order();
        order.setAccount(account);
        order.setReceiverName(req.receiverName().trim());
        order.setReceiverPhone(req.receiverPhone().trim());
        order.setShippingAddress(req.shippingAddress().trim());
        order.setNote(req.note() == null || req.note().isBlank() ? null : req.note().trim());

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : quantities.entrySet()) {
            Long productId = e.getKey();
            int qty = e.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id = " + productId));

            // Chỉ trừ khi còn đủ hàng, trả về 0 nếu không đủ
            if (productRepository.decreaseStock(productId, qty) == 0) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitPrice(product.getPrice());
            order.addItem(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        order.setTotalAmount(total);

        // Có lỗi ở bất kỳ sản phẩm nào thì cả giao dịch rollback, kho được hoàn lại
        return OrderResponse.from(orderRepository.save(order));
    }

    public List<OrderResponse> findByUsername(String username) {
        return orderRepository.findByAccountUsernameOrderByCreatedAtDesc(username).stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse getForUser(Long id, String username, boolean isAdmin) {
        return OrderResponse.from(findOwned(id, username, isAdmin));
    }

    @Transactional
    public OrderResponse cancelByOwner(Long id, String username) {
        Order order = findOwned(id, username, false);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only a PENDING order can be cancelled");
        }
        changeStatus(order, OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    // ===== Admin =====

    public List<OrderResponse> findAll(OrderStatus status) {
        List<Order> orders = (status == null)
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id = " + id));
        changeStatus(order, newStatus);
        return OrderResponse.from(order);
    }

    // ===== Nội bộ =====

    private Order findOwned(Long id, String username, boolean isAdmin) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id = " + id));
        // Đơn của người khác trả 404 để không lộ việc đơn đó có tồn tại
        if (!isAdmin && !order.getAccount().getUsername().equals(username)) {
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
            // Hoàn lại tồn kho
            for (OrderItem item : order.getItems()) {
                productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }
        order.setStatus(next);
    }

    public OrderResponse getById(Long id) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id = " + id));
        return OrderResponse.from(order);
    }
}