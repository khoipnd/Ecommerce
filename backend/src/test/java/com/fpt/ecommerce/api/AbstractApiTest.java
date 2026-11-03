package com.fpt.ecommerce.api;

// Import các thư viện cần thiết cho việc test Repository và Test API
import com.fpt.ecommerce.repository.CategoryRepository;
import com.fpt.ecommerce.repository.ProductRepository;
import com.fpt.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach; // Annotation để chạy hàm setup trước mỗi test
import org.springframework.beans.factory.annotation.Autowired; // Dùng để inject (tiêm) các bean của Spring
import org.springframework.boot.test.context.SpringBootTest; // Khởi động toàn bộ context của Spring Boot phục vụ cho test tích hợp
import org.springframework.http.MediaType; // Định nghĩa các kiểu dữ liệu gửi/nhận (như JSON)
import org.springframework.test.web.servlet.MockMvc; // Giả lập các HTTP Request để gọi API mà không cần chạy server thật
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder; // Dùng để xây dựng một request giả lập
import org.springframework.test.web.servlet.setup.MockMvcBuilders; // Class hỗ trợ tạo ra đối tượng MockMvc
import org.springframework.web.context.WebApplicationContext; // Quản lý toàn bộ cấu hình web và bean của Spring

// Import các cấu hình Security và Request giả lập
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Lớp nền cho các test API: dựng MockMvc (có Spring Security), dọn dữ liệu trước mỗi test.
 * Các request ghi (POST/PUT/DELETE) gắn sẵn user ADMIN + csrf để test vẫn đúng
 * khi Spring Security (BE-14) được siết quyền sau này.
 */
@SpringBootTest // Bật Spring Boot context để inject các repository và service thật
abstract class AbstractApiTest {

    // Tiêm (inject) WebApplicationContext để cấu hình MockMvc
    @Autowired
    protected WebApplicationContext context;
    
    // Tiêm CategoryRepository để hỗ trợ thao tác xoá hoặc chuẩn bị dữ liệu test
    @Autowired
    protected CategoryRepository categoryRepository;
    
    // Tiêm ProductRepository để thao tác với bảng product trong test
    @Autowired
    protected ProductRepository productRepository;
    
    // Tiêm OrderRepository để thao tác với bảng order
    @Autowired
    protected OrderRepository orderRepository;

    // Đối tượng chính để thực hiện các lời gọi API giả (mock requests) vào hệ thống
    protected MockMvc mockMvc;

    // Hàm này sẽ tự động chạy TRƯỚC MỖI phương thức test (@Test) ở các class kế thừa
    @BeforeEach
    void setUpMockMvc() {
        // Cấu hình MockMvc dựa trên WebApplicationContext và nhúng thêm bộ lọc Spring Security
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()) // Áp dụng security để kiểm tra quyền truy cập API như thật
                .build();
                
        // Dọn dẹp dữ liệu cũ (reset DB) trước khi chạy mỗi bài test để không bị nhiễu dữ liệu giữa các test
        orderRepository.deleteAll(); // Xóa đơn test trước sản phẩm để không vi phạm khóa ngoại.
        productRepository.deleteAll(); // Tiếp theo xoá hết sản phẩm
        categoryRepository.deleteAll(); // Cuối cùng xoá hết danh mục
    }

    // Hàm hỗ trợ (helper) tạo sẵn một request POST chứa body JSON
    protected MockHttpServletRequestBuilder postJson(String url, String json) {
        return post(url) // Tạo lệnh POST tới đường dẫn (url)
                .with(user("admin").roles("ADMIN")) // Giả lập quyền đang đăng nhập là ADMIN (bỏ qua bước đăng nhập thực tế)
                .with(csrf()) // Gửi kèm token CSRF để vượt qua bảo mật chống giả mạo request của Spring
                .contentType(MediaType.APPLICATION_JSON) // Đặt Content-Type của header là application/json
                .content(json); // Đính kèm chuỗi JSON dữ liệu vào body của request
    }

    // Hàm hỗ trợ tạo sẵn một request PUT chứa body JSON
    protected MockHttpServletRequestBuilder putJson(String url, String json) {
        return put(url) // Tạo lệnh PUT tới đường dẫn
                .with(user("admin").roles("ADMIN")) // Giả lập đang đăng nhập với quyền ADMIN
                .with(csrf()) // Bọc token CSRF để vượt rào bảo mật
                .contentType(MediaType.APPLICATION_JSON) // Khai báo dữ liệu gửi lên là JSON
                .content(json); // Nhét chuỗi JSON vào nội dung body
    }

    // Hàm hỗ trợ tạo sẵn một request DELETE (không cần body)
    protected MockHttpServletRequestBuilder deleteAsAdmin(String url) {
        return delete(url) // Tạo lệnh DELETE tới url
                .with(user("admin").roles("ADMIN")) // Uỷ quyền bằng tài khoản có role ADMIN
                .with(csrf()); // Bọc token bảo mật CSRF
    }
}
