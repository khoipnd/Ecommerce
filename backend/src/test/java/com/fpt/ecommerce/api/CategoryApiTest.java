package com.fpt.ecommerce.api;

// Nhập các Entity liên quan từ hệ thống
import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.entity.Product;
// Thư viện JUnit 5 để tuỳ chỉnh tên Test hiển thị cho dễ đọc
import org.junit.jupiter.api.DisplayName;
// Import annotation Test của JUnit
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

// Import hàm giả lập gửi request dạng GET
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// Import hàm kiểm tra nội dung JSON trả về
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// Import hàm kiểm tra mã trạng thái HTTP (HTTP status)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 
 * BE-07 Category REST API + BE-15 Testing 
 * Kế thừa AbstractApiTest để tái sử dụng cấu hình MockMvc, Security, và hàm xoá dữ liệu rác.
 */
class CategoryApiTest extends AbstractApiTest {

    // Đường dẫn gốc của API danh mục, được dùng chung cho các bài test bên dưới
    private static final String URL = "/api/v1/categories";

    @Test
    // Khai báo tên thân thiện cho bài Test
    @DisplayName("POST /categories -> 201 và trả về category vừa tạo")
    void createCategory() throws Exception {
        // Gửi request POST đến URL với dữ liệu JSON tạo danh mục "Electronics"
        mockMvc.perform(postJson(URL, "{\"name\":\"Electronics\"}"))
                // Mong đợi kết quả server trả về mã trạng thái 201 Created
                .andExpect(status().isCreated())
                // Mong đợi trong cục JSON trả về có trường "id" mang kiểu số (được tự động sinh)
                .andExpect(jsonPath("$.id").isNumber())
                // Và trường "name" mang đúng giá trị "Electronics" vừa gửi lên
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("POST /categories với name rỗng -> 400 kèm lỗi theo field")
    void createCategoryBlankName() throws Exception {
        // Cố tình gửi lên tên danh mục rỗng (toàn khoảng trắng)
        mockMvc.perform(postJson(URL, "{\"name\":\"  \"}"))
                // Mong đợi kết quả là BadRequest (400) vì hệ thống validation bắt được lỗi
                .andExpect(status().isBadRequest())
                // Mong đợi server sẽ phản hồi cho biết chi tiết rằng trường bị lỗi là trường "name"
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    @DisplayName("POST /categories trùng tên (không phân biệt hoa thường) -> 400")
    void createCategoryDuplicate() throws Exception {
        // Trước tiên, cấy vào database một danh mục có tên "Books"
        categoryRepository.save(new Category("Books"));

        // Sau đó cố tình tạo tiếp một danh mục tên "books" (viết thường chữ b)
        mockMvc.perform(postJson(URL, "{\"name\":\"books\"}"))
                // Mong đợi lỗi 400 BadRequest do vi phạm luật tên unique
                .andExpect(status().isBadRequest())
                // Mong đợi cấu trúc trả về có chứa trường báo lỗi chung "error"
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /categories -> danh sách")
    void getAllCategories() throws Exception {
        // Khởi tạo sẵn hai danh mục và lưu thẳng vào DB
        categoryRepository.save(new Category("Books"));
        categoryRepository.save(new Category("Clothing"));

        // Gửi request GET (lấy tất cả danh mục)
        mockMvc.perform(get(URL))
                // Mong đợi HTTP 200 OK
                .andExpect(status().isOk())
                // Mong đợi mảng JSON trả về có chiều dài đúng bằng 2 (vì đã add 2 phần tử ở trên)
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /categories/{id} -> 200; id không tồn tại -> 404")
    void getCategoryById() throws Exception {
        // Lưu sẵn một category để lấy ID thật
        Category saved = categoryRepository.save(new Category("Books"));

        // Test luồng đúng: Gửi GET với id hợp lệ
        mockMvc.perform(get(URL + "/" + saved.getId()))
                // Mong đợi kết quả là OK
                .andExpect(status().isOk())
                // Lấy ra phải đúng tên "Books"
                .andExpect(jsonPath("$.name").value("Books"));

        // Test luồng sai: Gửi GET với id không có thật trên DB
        mockMvc.perform(get(URL + "/999999"))
                // Hệ thống phải báo NotFound (404)
                .andExpect(status().isNotFound())
                // Trả về JSON chứa field "error"
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /categories/abc -> 400 (sai kiểu id)")
    void getCategoryInvalidId() throws Exception {
        // Gửi tham số ID trên URL không phải số mà là chuỗi chữ cái "abc"
        mockMvc.perform(get(URL + "/abc"))
                // Lỗi type mismatch của Spring phải trả về BadRequest 400, không được văng 500
                .andExpect(status().isBadRequest())
                // JSON cũng chứa field "error"
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /categories/{id} -> cập nhật tên")
    void updateCategory() throws Exception {
        // Lưu trước một danh mục ban đầu
        Category saved = categoryRepository.save(new Category("Books"));

        // Dùng PUT request sửa tên nó thành "Novels"
        mockMvc.perform(putJson(URL + "/" + saved.getId(), "{\"name\":\"Novels\"}"))
                // Mong đợi cập nhật thành công (200 OK)
                .andExpect(status().isOk())
                // Phản hồi trả về phải lấy tên mới nhất
                .andExpect(jsonPath("$.name").value("Novels"));
    }

    @Test
    @DisplayName("DELETE /categories/{id} -> 204, sau đó GET -> 404")
    void deleteCategory() throws Exception {
        // Lưu một danh mục để chuẩn bị đem đi xoá
        Category saved = categoryRepository.save(new Category("Books"));

        // Thực thi lệnh DELETE với quyền admin
        mockMvc.perform(deleteAsAdmin(URL + "/" + saved.getId()))
                // Sau khi xoá thành công, mã trạng thái HTTP chuẩn của REST là 204 No Content
                .andExpect(status().isNoContent());

        // Kiểm tra xem thực sự nó đã bay khỏi DB chưa bằng cách lấy lại nó
        mockMvc.perform(get(URL + "/" + saved.getId()))
                // Kết quả chắc chắn phải ra 404 (do không còn tồn tại)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE category đang có product -> 409 (không còn 500)")
    void deleteCategoryInUse() throws Exception {
        // 1. Lưu một danh mục
        Category saved = categoryRepository.save(new Category("Books"));
        // 2. Lưu một sản phẩm (product) nhưng liên kết trực tiếp tới danh mục vừa rồi
        productRepository.save(new Product(null, "Java Core", "desc",
                new BigDecimal("150.00"), 5, saved));

        // 3. Admin cố tình ấn nút xoá danh mục "Books" 
        mockMvc.perform(deleteAsAdmin(URL + "/" + saved.getId()))
                // 4. Vì danh mục đang bị sản phẩm dùng -> Vi phạm khoá ngoại -> 
                // Thay vì ném lỗi hệ thống 500, ControllerAdvice sẽ bắt lại và biến thành lỗi 409 Conflict (từ chối xoá)
                .andExpect(status().isConflict())
                // 5. Khẳng định trong response sẽ giải thích lý do không xoá được ở thuộc tính "error"
                .andExpect(jsonPath("$.error").exists());
    }
}
