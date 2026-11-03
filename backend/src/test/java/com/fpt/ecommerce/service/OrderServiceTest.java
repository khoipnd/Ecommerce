package com.fpt.ecommerce.service;

// Import các DTO
import com.fpt.ecommerce.dto.OrderItemRequest;
import com.fpt.ecommerce.dto.OrderRequest;
import com.fpt.ecommerce.dto.OrderResponse;
// Import các Entity
import com.fpt.ecommerce.entity.Account;
import com.fpt.ecommerce.entity.Product;
// Import các Repository
import com.fpt.ecommerce.repository.AccountRepository;
import com.fpt.ecommerce.repository.OrderRepository;
import com.fpt.ecommerce.repository.ProductRepository;
// Import JUnit & Mockito Annotations
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Import các hàm kiểm tra kết quả
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// Import các hàm giả lập của Mockito
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Kích hoạt Mockito để tạo mock object trong các test
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // 1. Tạo các bản sao giả (Mock) cho các Repository (Để không cần kết nối DB thật)
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private AccountRepository accountRepository;

    // 2. Tự động tiêm (Inject) các Mock Repository ở trên vào trong OrderService
    @InjectMocks
    private OrderService orderService;

    /**
     * Kiểm tra chức năng tạo đơn hàng, đảm bảo tổng tiền được tính chính xác dựa trên số lượng và giá sản phẩm.
     */
    @Test
    void create_ShouldCalculateTotalAmountCorrectly() {
        // [ARRANGE - CHUẨN BỊ]
        String username = "testuser";
        Account account = new Account();
        account.setUsername(username);
        // Cấu hình mock: Khi hệ thống tìm tài khoản của "testuser", hãy trả về tài khoản giả định
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

        // Chuẩn bị giả lập sản phẩm số 1 (Giá 100)
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        // Cấu hình mock: Khi mua 2 cái Product 1 (tức là giảm stock đi 2), DB sẽ update thành công (trả về 1 dòng ảnh hưởng)
        when(productRepository.decreaseStock(1L, 2)).thenReturn(1);

        // Chuẩn bị giả lập sản phẩm số 2 (Giá 50)
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("50.00"));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        // Cấu hình mock: Khi mua 1 cái Product 2, giảm stock thành công
        when(productRepository.decreaseStock(2L, 1)).thenReturn(1);

        // Tạo ra gói dữ liệu mua hàng gửi từ Frontend
        OrderRequest request = new OrderRequest(
                "John Doe", "1234567890", "123 Main St", "Note",
                List.of(
                        new OrderItemRequest(1L, 2), // Mua 2 cái SP 1 (2 * 100 = 200)
                        new OrderItemRequest(2L, 1)  // Mua 1 cái SP 2 (1 * 50 = 50)
                )
        );

        // Cấu hình mock: Chặn ngay lúc DB chuẩn bị lưu đơn hàng để gán ID và Trạng thái ảo cho nó
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            com.fpt.ecommerce.entity.Order o = invocation.getArgument(0);
            o.setId(100L); // Giả vờ DB đã tạo ID là 100
            o.setStatus(com.fpt.ecommerce.entity.OrderStatus.PENDING); // Status mặc định
            return o;
        });

        // [ACT - THỰC THI]
        // Kích hoạt hàm tạo đơn hàng trong service
        OrderResponse response = orderService.create(username, request);

        // [ASSERT - KIỂM TRA]
        // Đảm bảo đơn hàng được tạo thành công (không bị lỗi trả về null)
        assertNotNull(response);
        // Tổng tiền (TotalAmount) phải bằng chính xác: 200 + 50 = 250
        // Dùng compareTo thay vì equals đối với BigDecimal để so sánh giá trị toán học (bỏ qua độ dài thập phân nếu có)
        assertEquals(0, new BigDecimal("250.00").compareTo(response.totalAmount()));
    }

    /**
     * Kiểm tra chức năng tìm danh sách đơn hàng dựa trên username của người dùng.
     */
    @Test
    void findByUsername_ShouldReturnOrders() {
        // [ARRANGE - CHUẨN BỊ]
        // Chuẩn bị một đơn hàng thuộc về người dùng "testuser"
        com.fpt.ecommerce.entity.Order order = new com.fpt.ecommerce.entity.Order();
        order.setId(1L);
        Account account = new Account();
        account.setUsername("testuser");
        order.setAccount(account);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(com.fpt.ecommerce.entity.OrderStatus.PENDING);

        // Cấu hình mock: Khi DB quét tìm các đơn của "testuser", trả về List có chứa đơn hàng này
        when(orderRepository.findByAccountUsernameOrderByCreatedAtDesc("testuser")).thenReturn(List.of(order));

        // [ACT - THỰC THI]
        List<OrderResponse> result = orderService.findByUsername("testuser");
        
        // [ASSERT - KIỂM TRA]
        // Bắt buộc phải có đúng 1 đơn hàng trong danh sách
        assertEquals(1, result.size());
        // ID đơn hàng đầu tiên phải đúng là 1
        assertEquals(1L, result.get(0).id());
    }

    /**
     * Kiểm tra chức năng cập nhật trạng thái đơn hàng của hệ thống (dành cho Admin).
     */
    @Test
    void updateStatus_ShouldUpdateAndReturnOrder() {
        // [ARRANGE - CHUẨN BỊ]
        // Tạo một đơn hàng đang ở trạng thái mặc định (PENDING)
        com.fpt.ecommerce.entity.Order order = new com.fpt.ecommerce.entity.Order();
        order.setId(1L);
        Account account = new Account();
        account.setUsername("testuser");
        order.setAccount(account);
        order.setStatus(com.fpt.ecommerce.entity.OrderStatus.PENDING);

        // Cấu hình mock: Giả lập việc tìm thấy đơn hàng ID = 1 trong DB
        when(orderRepository.findWithDetailsById(1L)).thenReturn(Optional.of(order));

        // [ACT - THỰC THI]
        // Admin cập nhật chuyển từ PENDING sang CONFIRMED
        OrderResponse response = orderService.updateStatus(1L, com.fpt.ecommerce.entity.OrderStatus.CONFIRMED);
        
        // [ASSERT - KIỂM TRA]
        // Trạng thái đơn hàng trong response trả về phải biến thành CONFIRMED
        assertEquals(com.fpt.ecommerce.entity.OrderStatus.CONFIRMED, response.status());
    }

    /**
     * Kiểm tra chức năng người dùng xem chi tiết đơn hàng của CHÍNH MÌNH (bảo mật dữ liệu cá nhân).
     */
    @Test
    void getForUser_ShouldReturnOrder() {
        // [ARRANGE - CHUẨN BỊ]
        com.fpt.ecommerce.entity.Order order = new com.fpt.ecommerce.entity.Order();
        order.setId(1L);
        Account account = new Account();
        account.setUsername("testuser"); // Chủ sở hữu đơn hàng
        order.setAccount(account);
        order.setStatus(com.fpt.ecommerce.entity.OrderStatus.PENDING);

        // Cấu hình mock: Trả về đơn hàng số 1
        when(orderRepository.findWithDetailsById(1L)).thenReturn(Optional.of(order));

        // [ACT - THỰC THI]
        // Cố gắng mở đơn hàng số 1 với tư cách là "testuser"
        OrderResponse response = orderService.getForUser(1L, "testuser", false);
        
        // [ASSERT - KIỂM TRA]
        // Vì đúng chủ nhân, hệ thống cho phép xem -> Trả về ID 1
        assertEquals(1L, response.id());
    }

    /**
     * Kiểm tra chức năng người dùng tự huỷ đơn hàng.
     */
    @Test
    void cancelByOwner_ShouldCancelOrder() {
        // [ARRANGE - CHUẨN BỊ]
        com.fpt.ecommerce.entity.Order order = new com.fpt.ecommerce.entity.Order();
        order.setId(1L);
        Account account = new Account();
        account.setUsername("testuser"); // Người huỷ phải chính là chủ đơn
        order.setAccount(account);
        order.setStatus(com.fpt.ecommerce.entity.OrderStatus.PENDING); // Phải ở trạng thái PENDING thì mới được huỷ

        // Cấu hình mock: Tìm thấy đơn hàng
        when(orderRepository.findWithDetailsById(1L)).thenReturn(Optional.of(order));

        // [ACT - THỰC THI]
        // Gọi hàm huỷ đơn bởi chủ sở hữu
        OrderResponse response = orderService.cancelByOwner(1L, "testuser");
        
        // [ASSERT - KIỂM TRA]
        // Trạng thái đơn hàng phải bị thay đổi thành CANCELLED
        assertEquals(com.fpt.ecommerce.entity.OrderStatus.CANCELLED, response.status());
    }

    /**
     * Kiểm tra lấy chi tiết đơn hàng qua id dành cho quyền admin (Admin có thể xem đơn của bất kỳ ai).
     */
    @Test
    void getById_ShouldReturnOrder() {
        // [ARRANGE - CHUẨN BỊ]
        com.fpt.ecommerce.entity.Order order = new com.fpt.ecommerce.entity.Order();
        order.setId(1L);
        Account account = new Account();
        account.setUsername("admin");
        order.setAccount(account);
        order.setStatus(com.fpt.ecommerce.entity.OrderStatus.COMPLETED);

        // Cấu hình mock: Trả về đơn hàng số 1
        when(orderRepository.findWithDetailsById(1L)).thenReturn(Optional.of(order));

        // [ACT - THỰC THI]
        // Gọi hàm xem (không quan tâm chủ nhân đơn hàng là ai)
        OrderResponse response = orderService.getById(1L);
        
        // [ASSERT - KIỂM TRA]
        // Phải trả về thông tin thành công
        assertEquals(1L, response.id());
    }
}
