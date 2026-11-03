package com.fpt.ecommerce.api;

// Nhập các Entity liên quan từ hệ thống
import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.entity.Product;
// Import annotation của JUnit để thực thi hàm trước mỗi bài Test
import org.junit.jupiter.api.BeforeEach;
// Import annotation để đặt tên hiển thị cho bài Test dễ đọc hơn
import org.junit.jupiter.api.DisplayName;
// Import annotation đánh dấu một phương thức là một bài Test
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

// Import hàm giả lập gửi request HTTP GET
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// Import hàm kiểm tra nội dung JSON trả về theo đường dẫn (JSON Path)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// Import hàm kiểm tra mã trạng thái HTTP (status code) trả về
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 
 * BE-08 Product REST API + BE-15 Testing 
 * Kế thừa AbstractApiTest để dùng chung cấu hình MockMvc, Security, dọn DB.
 */
class ProductApiTest extends AbstractApiTest {

    // Khai báo đường dẫn gốc của API sản phẩm để tái sử dụng
    private static final String URL = "/api/v1/products";

    // Khai báo một biến category dùng chung cho việc tạo sản phẩm trong các test
    private Category category;

    // Annotation này giúp hàm chạy tự động TRƯỚC MỖI bài @Test trong file này
    @BeforeEach
    void seedCategory() {
        // Tạo sẵn một danh mục "Electronics" trong CSDL vì Product bắt buộc phải thuộc về 1 Category
        category = categoryRepository.save(new Category("Electronics"));
    }

    // Hàm hỗ trợ (helper): Trả về chuỗi JSON đại diện cho dữ liệu tạo/sửa Product
    private String productJson(String name, String price, int stock, Long categoryId) {
        // Nối chuỗi tạo ra định dạng JSON khớp với cấu trúc ProductRequest DTO
        return "{\"name\":\"" + name + "\",\"description\":\"desc\",\"price\":" + price
                + ",\"stockQuantity\":" + stock + ",\"category\":{\"id\":" + categoryId + "}}";
    }

    // Hàm hỗ trợ: Lưu nhanh một sản phẩm hợp lệ vào thẳng CSDL (bỏ qua API) để chuẩn bị data
    private Product savedProduct(String name) {
        return productRepository.save(new Product(null, name, "desc",
                new BigDecimal("100.00"), 5, category));
    }

    @Test
    @DisplayName("POST /products -> 201 và có category")
    void createProduct() throws Exception {
        // Gửi request POST tạo mới 1 sản phẩm kèm dữ liệu JSON hợp lệ
        mockMvc.perform(postJson(URL, productJson("Phone", "199.99", 5, category.getId())))
                // Phải trả về mã trạng thái 201 Created (Tạo thành công)
                .andExpect(status().isCreated())
                // Kiểm tra JSON trả về: Trường "id" phải là một con số sinh tự động
                .andExpect(jsonPath("$.id").isNumber())
                // Tên sản phẩm trả về phải khớp với dữ liệu gửi lên
                .andExpect(jsonPath("$.name").value("Phone"))
                // Giá trả về phải khớp với dữ liệu gửi lên
                .andExpect(jsonPath("$.price").value(199.99))
                // ID của danh mục trả về phải khớp với ID danh mục đã truyền vào
                .andExpect(jsonPath("$.category.id").value(category.getId()));
    }

    @Test
    @DisplayName("POST /products dữ liệu sai (name rỗng, price 0, stock âm) -> 400 theo field")
    void createProductInvalid() throws Exception {
        // Cố tình gửi request tạo sản phẩm với dữ liệu KHÔNG HỢP LỆ: Tên rỗng, Giá = 0, Số lượng = -1
        mockMvc.perform(postJson(URL, productJson("", "0", -1, category.getId())))
                // Phải bị Validation chặn lại và trả về mã 400 BadRequest
                .andExpect(status().isBadRequest())
                // Hệ thống phải báo chính xác các trường bị lỗi trong response JSON
                .andExpect(jsonPath("$.name").exists()) // Có báo lỗi trường name
                .andExpect(jsonPath("$.price").exists()) // Có báo lỗi trường price
                .andExpect(jsonPath("$.stockQuantity").exists()); // Có báo lỗi trường stockQuantity
    }

    @Test
    @DisplayName("POST /products với category không tồn tại -> 404 {error}")
    void createProductCategoryNotFound() throws Exception {
        // Cố tình gửi request tạo sản phẩm thuộc về một category ID ảo (999999L)
        mockMvc.perform(postJson(URL, productJson("Phone", "10", 1, 999999L)))
                // Hệ thống phải trả về mã 404 NotFound vì không tìm thấy danh mục để liên kết
                .andExpect(status().isNotFound())
                // Phải có thông báo lỗi chung trong trường "error"
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /products JSON sai định dạng -> 400 {error}")
    void createProductMalformedJson() throws Exception {
        // Cố tình gửi body request là một chuỗi không phải định dạng JSON chuẩn
        mockMvc.perform(postJson(URL, "{bad json"))
                // Lỗi parse JSON của Spring phải được bắt và trả về 400 BadRequest
                .andExpect(status().isBadRequest())
                // Phải có mô tả lỗi
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /products và GET /products/{id}")
    void getProducts() throws Exception {
        // Chuẩn bị 2 sản phẩm trong CSDL
        Product p = savedProduct("Phone");
        savedProduct("Laptop");

        // Gọi API GET lấy danh sách TẤT CẢ sản phẩm
        mockMvc.perform(get(URL))
                // Phải trả về mã 200 OK
                .andExpect(status().isOk())
                // Mảng trả về phải có đúng 2 phần tử
                .andExpect(jsonPath("$.length()").value(2));

        // Gọi API GET lấy CHI TIẾT 1 sản phẩm cụ thể theo ID
        mockMvc.perform(get(URL + "/" + p.getId()))
                // Phải trả về 200 OK
                .andExpect(status().isOk())
                // Tên sản phẩm lấy ra phải khớp với "Phone"
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    @DisplayName("GET /products/{id} không tồn tại -> 404 {error}")
    void getProductNotFound() throws Exception {
        // Cố tình gọi API lấy chi tiết sản phẩm với 1 ID không có thật
        mockMvc.perform(get(URL + "/999999"))
                // Hệ thống phải trả về mã 404 NotFound
                .andExpect(status().isNotFound())
                // Có chứa thông điệp lỗi
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("PUT /products/{id} -> cập nhật")
    void updateProduct() throws Exception {
        // Tạo sẵn một sản phẩm gốc tên "Phone"
        Product p = savedProduct("Phone");

        // Gọi API PUT để cập nhật lại thông tin (Sửa tên thành "Phone v2", đổi giá thành 250, tồn kho thành 9)
        mockMvc.perform(putJson(URL + "/" + p.getId(),
                        productJson("Phone v2", "250.00", 9, category.getId())))
                // Phải thành công (200 OK)
                .andExpect(status().isOk())
                // Xác nhận tên đã được cập nhật
                .andExpect(jsonPath("$.name").value("Phone v2"))
                // Xác nhận số lượng tồn kho đã được cập nhật
                .andExpect(jsonPath("$.stockQuantity").value(9));
    }

    @Test
    @DisplayName("DELETE /products/{id} -> 204, sau đó GET -> 404")
    void deleteProduct() throws Exception {
        // Tạo một sản phẩm để chuẩn bị đem xoá
        Product p = savedProduct("Phone");

        // Dùng tài khoản admin gọi API DELETE để xoá sản phẩm
        mockMvc.perform(deleteAsAdmin(URL + "/" + p.getId()))
                // Xoá thành công phải trả về 204 No Content
                .andExpect(status().isNoContent());

        // Cố gắng gọi lại API GET lấy sản phẩm vừa xoá
        mockMvc.perform(get(URL + "/" + p.getId()))
                // Phải trả về 404 NotFound vì sản phẩm không còn tồn tại trên DB
                .andExpect(status().isNotFound());
    }
}
