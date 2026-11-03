package com.fpt.ecommerce.api;

// Nhập các lớp Entity cần thiết để thao tác với CSDL
import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.entity.Product;
// Thư viện JsonPath dùng để đọc/lấy dữ liệu từ chuỗi JSON trả về
import com.jayway.jsonpath.JsonPath;
// Thư viện JUnit 5
import org.junit.jupiter.api.Test;
// Các cấu hình liên quan đến Media Types (VD: application/json)
import org.springframework.http.MediaType;

import java.math.BigDecimal;

// Import các hàm Assert (xác nhận) của JUnit
import static org.junit.jupiter.api.Assertions.assertEquals;
// Import các hàm tạo request mô phỏng của Spring (GET, POST, PUT, DELETE, PATCH, OPTIONS...)
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// Import các hàm để đối chiếu kết quả trả về (status, jsonPath, header)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 
 * Kiểm tra việc xác thực bằng JWT thực tế qua giao thức HTTP. 
 * Khác với việc giả lập (mock) authentication bằng with(user("...")), lớp này sẽ test toàn bộ 
 * quy trình từ lúc gửi email/mật khẩu -> lấy Token thật -> dùng Token để gọi API.
 */
class AuthOrderApiTest extends AbstractApiTest {
    
    // Hàm hỗ trợ (helper): Tạo một User ngẫu nhiên (để test) thông qua việc gọi API register
    private String registerUser() throws Exception {
        // Tạo một email ngẫu nhiên dựa trên thời gian hệ thống để không bị trùng lặp
        String email = "rest-" + System.nanoTime() + "@example.com";
        // Gọi API POST /api/v1/auth/register
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        // Gửi kèm payload JSON. Lưu ý: Dù user cố tình gán role là ADMIN...
                        .content("{\"email\":\"" + email + "\",\"password\":\"test123456\",\"role\":\"ADMIN\"}"))
                // Hệ thống vẫn trả về 201 (Created)
                .andExpect(status().isCreated())
                // Nhưng role thực tế cấp phát phải là USER (bảo mật không cho người lạ tự tạo quyền ADMIN)
                .andExpect(jsonPath("$.role").value("USER"));
        // Trả về email vừa tạo để dùng cho việc login
        return email;
    }

    // Hàm hỗ trợ: Đăng nhập và lấy ra JWT Token thật
    private String token(String identifier, String password) throws Exception {
        // Gọi API POST /api/v1/auth/login với thông tin tài khoản
        String json = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}"))
                // Mong đợi kết quả là HTTP 200 OK
                .andExpect(status().isOk())
                // Lấy ra nội dung chuỗi JSON trong body trả về
                .andReturn().getResponse().getContentAsString();
        // Trích xuất trường "token" từ trong JSON đó và trả ra ngoài
        return JsonPath.read(json, "$.token");
    }

    // Hàm hỗ trợ: Chuẩn bị 1 Product trong Database để phục vụ việc test đặt hàng
    private Product product() {
        // Tạo một Category mới tên là "REST orders" và lưu xuống DB
        Category category = categoryRepository.save(new Category("REST orders"));
        // Tạo một Product mới có giá 100.00, số lượng tồn (stock) = 5 và thuộc Category vừa tạo
        return productRepository.save(new Product(null, "REST phone", "test", new BigDecimal("100.00"), 5, category));
    }

    // Hàm hỗ trợ: Trả ra chuỗi JSON mô phỏng một body request đặt hàng (OrderRequest)
    private String body(Long id, int quantity) {
        // Sử dụng Java Text Block (Multi-line string) để viết JSON cho gọn
        return """
                {"receiverName":"Test User","receiverPhone":"0901234567","shippingAddress":"Test address",
                 "items":[{"productId":%d,"quantity":%d}],"totalAmount":1}
                """.formatted(id, quantity);
    }

    // Hàm hỗ trợ: Đặt một đơn hàng thực tế
    private long create(String token, Product product) throws Exception {
        // Gọi API POST /api/v1/orders
        String json = mockMvc.perform(post("/api/v1/orders")
                        // Đính kèm token JWT vào header Authorization
                        .header("Authorization", "Bearer " + token)
                        // Khai báo kiểu content JSON và truyền payload body đặt hàng (số lượng mua: 2)
                        .contentType(MediaType.APPLICATION_JSON).content(body(product.getId(), 2)))
                // Chờ đợi kết quả HTTP 201 Created
                .andExpect(status().isCreated())
                // Chờ đợi trạng thái mặc định của đơn là PENDING
                .andExpect(jsonPath("$.status").value("PENDING"))
                // Tổng tiền mua 2 sản phẩm giá 100 phải bằng 200 (Dù payload client tự gửi totalAmount=1 thì server tính lại là 200)
                .andExpect(jsonPath("$.totalAmount").value(200))
                // Kiểm tra xem thời gian tạo có không rỗng
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                // Lấy ra chuỗi response body
                .andReturn().getResponse().getContentAsString();
        // Trả về ID của đơn hàng vừa tạo (lấy thông qua jsonPath $.id)
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    // [TEST CASE 1]: Kiểm tra luồng đăng ký, đăng nhập và phân quyền truy cập
    @Test
    void registrationLoginAndAccessControl() throws Exception {
        // 1. Đăng ký một user mới
        String email = registerUser();
        // 2. Đăng nhập để lấy token của user đó
        String jwt = token(email, "test123456");
        
        // 3. Test lấy thông tin cá nhân: Cung cấp token và lấy được đúng email
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
                
        // 4. Test đăng ký lại bằng email cũ -> báo lỗi Conflict (409)
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"test123456\"}"))
                .andExpect(status().isConflict());
                
        // 5. Test đăng nhập sai mật khẩu -> báo lỗi Unauthorized (401)
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"" + email + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
                
        // 6. Test xem đơn hàng mà không đính kèm token -> báo lỗi Unauthorized (401)
        mockMvc.perform(get("/api/v1/orders/my")).andExpect(status().isUnauthorized());
        
        // 7. Test xem đơn hàng mà đính kèm token bậy -> báo lỗi Unauthorized (401)
        mockMvc.perform(get("/api/v1/orders/my").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
                
        // 8. Test lấy toàn bộ đơn hàng (của Admin) bằng quyền User thường -> báo lỗi Forbidden (403)
        mockMvc.perform(get("/api/v1/admin/orders").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
                
        // 9. Test tạo sản phẩm bằng quyền User thường -> báo lỗi Forbidden (403)
        mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
                
        // 10. Test xem danh sách sản phẩm (không cần đăng nhập) -> trả về OK (200)
        mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk());
    }

    // [TEST CASE 2]: Kiểm tra người mua có thể tự huỷ đơn, hoàn lại stock, và không thể xem đơn người khác
    @Test
    void ownerCancelRestoresStockAndOtherUsersCannotRead() throws Exception {
        // Tạo User thứ nhất và lấy token
        String jwt = token(registerUser(), "test123456");
        // Khởi tạo một sản phẩm mẫu (có số lượng tồn là 5)
        Product product = product();
        // User thứ 1 tiến hành đặt hàng mua 2 sản phẩm (còn 3)
        long id = create(jwt, product);
        
        // Xác nhận số lượng tồn kho (stock) hiện đã giảm xuống còn 3
        assertEquals(3, productRepository.findById(product.getId()).orElseThrow().getStockQuantity());
        
        // User thứ 1 xem danh sách đơn hàng cá nhân, thấy được ID của đơn mình vừa tạo
        mockMvc.perform(get("/api/v1/orders/my").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
                
        // Tạo User thứ 2 (other) và lấy token
        String other = token(registerUser(), "test123456");
        
        // User thứ 2 cố tình xem chi tiết đơn hàng của User thứ 1 -> Báo lỗi NotFound (ẩn dữ liệu chéo)
        mockMvc.perform(get("/api/v1/orders/" + id).header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
                
        // User thứ 2 cố tình huỷ đơn hàng của User thứ 1 -> Báo lỗi NotFound
        mockMvc.perform(patch("/api/v1/orders/" + id + "/cancel").header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
                
        // User thứ 1 tự huỷ đơn hàng của chính mình -> OK, status đổi thành CANCELLED
        mockMvc.perform(patch("/api/v1/orders/" + id + "/cancel").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
                
        // User thứ 1 lại cố tình huỷ lại đơn đã huỷ -> Báo lỗi BadRequest
        mockMvc.perform(patch("/api/v1/orders/" + id + "/cancel").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest());
                
        // Khi đơn hàng huỷ thành công, stock kho phải được cộng trả lại thành 5
        assertEquals(5, productRepository.findById(product.getId()).orElseThrow().getStockQuantity());
    }

    // [TEST CASE 3]: Kiểm tra việc chuyển trạng thái của Admin, và cấu hình CORS của Spring
    @Test
    void adminTransitionsAndPatchCors() throws Exception {
        // User thường tạo đơn hàng
        String jwt = token(registerUser(), "test123456");
        long id = create(jwt, product());
        
        // Đăng nhập tài khoản admin mặc định của hệ thống
        String admin = token("admin", "123456");
        
        // Admin xem chi tiết đơn hàng (bất chấp ai tạo) -> OK
        mockMvc.perform(get("/api/v1/admin/orders/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
                
        // Admin nhảy cóc trạng thái từ PENDING thẳng tới COMPLETED -> BadRequest (Lỗi Logic quy trình)
        mockMvc.perform(patch("/api/v1/admin/orders/" + id + "/status").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isBadRequest());
                
        // Admin thay đổi trạng thái theo đúng thứ tự logic hợp lệ: CONFIRMED -> SHIPPED -> COMPLETED
        for (String next : new String[]{"CONFIRMED", "SHIPPED", "COMPLETED"}) {
            mockMvc.perform(patch("/api/v1/admin/orders/" + id + "/status").header("Authorization", "Bearer " + admin)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + next + "\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(next));
        }
        
        // Lấy danh sách các đơn đã COMPLETED -> Phải ra được số lượng là 1 đơn vừa sửa
        mockMvc.perform(get("/api/v1/admin/orders?status=COMPLETED").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
                
        // Lúc này đơn hàng đã là COMPLETED, nếu User cố tình huỷ thì sẽ thất bại (BadRequest)
        mockMvc.perform(patch("/api/v1/orders/" + id + "/cancel").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest());
                
        // Kiểm tra request OPTIONS CORS (Frontend gửi pre-flight xem có quyền chạy hàm PATCH không)
        mockMvc.perform(options("/api/v1/admin/orders/" + id + "/status")
                        .header("Origin", "http://localhost:5173") // giả lập gửi từ React Frontend
                        .header("Access-Control-Request-Method", "PATCH") // Xin quyền chạy PATCH
                        .header("Access-Control-Request-Headers", "authorization,content-type")) // Xin các Headers này
                // Phải trả về OK 200 và header cho phép domain này.
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    // [TEST CASE 4]: Khi số lượng trong kho không đủ, phải rollback không tạo được đơn hàng
    @Test
    void insufficientStockRollsBackEntireOrder() throws Exception {
        String jwt = token(registerUser(), "test123456");
        Product first = product(); // Có 5 stock
        // Tạo sản phẩm 2 chỉ có đúng 1 stock
        Product second = productRepository.save(new Product(null, "Other phone", "test", new BigDecimal("200"), 1, first.getCategory()));
        
        // Khách hàng đặt mua 2 sản phẩm 1 và 2 sản phẩm 2 (sẽ vượt quá 1 stock của SP2)
        String json = """
                {"receiverName":"Test","receiverPhone":"0901234567","shippingAddress":"Address",
                 "items":[{"productId":%d,"quantity":2},{"productId":%d,"quantity":2}]}
                """.formatted(first.getId(), second.getId());
                
        // Gửi đơn hàng -> Bị BadRequest do không đủ hàng cho SP2
        mockMvc.perform(post("/api/v1/orders").header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());
                
        // Khẳng định lại là toàn bộ giao dịch mua bán bị huỷ, stock SP1 vẫn nguyên là 5
        assertEquals(5, productRepository.findById(first.getId()).orElseThrow().getStockQuantity());
        // Và đơn hàng hoàn toàn không được tạo ra trong DB (số lượng = 0)
        assertEquals(0, orderRepository.count());
    }
}
