package com.fpt.ecommerce.api;

// Nhập các Entity liên quan để chuẩn bị dữ liệu mẫu
import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.entity.Product;
// Các Annotation của JUnit
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

// Import hàm giả lập gửi request HTTP GET
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// Import hàm kiểm tra nội dung JSON trả về theo đường dẫn (JSON Path)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// Import hàm kiểm tra mã trạng thái HTTP (status code) trả về
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BE-10 Filter Products + BE-11 Pagination.
 * Kế thừa AbstractApiTest để có sẵn môi trường MockMvc.
 * Dữ liệu mẫu sẽ được tạo tự động: 
 *  - 12 "Phone 01..12" (thuộc nhóm Electronics, giá 100..1200, cứ sản phẩm thứ i chia hết cho 3 thì set hết hàng).
 *  - 5 "Java Book 1..5" (thuộc nhóm Books, giá 10..50, còn hàng).
 */
class ProductFilterApiTest extends AbstractApiTest {

    // Khai báo đường dẫn gốc của API lọc sản phẩm
    private static final String URL = "/api/v1/products/filter";

    // Khai báo sẵn hai danh mục sẽ được dùng làm dữ liệu mẫu
    private Category electronics;
    private Category books;

    // Chạy tự động trước mỗi bài Test để tạo sẵn dữ liệu
    @BeforeEach
    void seedProducts() {
        // Lưu danh mục Electronics vào DB
        electronics = categoryRepository.save(new Category("Electronics"));
        // Lưu danh mục Books vào DB
        books = categoryRepository.save(new Category("Books"));

        // Vòng lặp tạo 12 sản phẩm điện thoại
        for (int i = 1; i <= 12; i++) {
            // Lưu từng sản phẩm với logic: Giá = i * 100. Tồn kho = nếu i chia hết cho 3 thì 0 (hết hàng), ngược lại là 10.
            productRepository.save(new Product(null, String.format("Phone %02d", i), "phone",
                    new BigDecimal(100 * i).setScale(2), i % 3 == 0 ? 0 : 10, electronics));
        }
        
        // Vòng lặp tạo 5 sản phẩm sách
        for (int i = 1; i <= 5; i++) {
            // Giá = i * 10. Số lượng tồn kho cố định là 5
            productRepository.save(new Product(null, "Java Book " + i, "book",
                    new BigDecimal(10 * i).setScale(2), 5, books));
        }
    }

    @Test
    @DisplayName("Mặc định: trang 0, size 10, tổng 17 sản phẩm / 2 trang")
    void defaultPagination() throws Exception {
        // Gửi GET /api/v1/products/filter mà KHÔNG truyền thêm tham số nào (sẽ lấy mặc định)
        mockMvc.perform(get(URL))
                // Phải trả về 200 OK
                .andExpect(status().isOk())
                // Mặc định phân trang trả về 10 sản phẩm (size mặc định)
                .andExpect(jsonPath("$.content.length()").value(10))
                // Mặc định là đang ở trang 0 (trang đầu tiên)
                .andExpect(jsonPath("$.page").value(0))
                // Mặc định size là 10
                .andExpect(jsonPath("$.size").value(10))
                // Tổng số lượng sản phẩm trên toàn DB là 17
                .andExpect(jsonPath("$.totalElements").value(17))
                // Tổng số trang = 2 (do 17 chia cho 10 dư 7)
                .andExpect(jsonPath("$.totalPages").value(2))
                // Đây đúng là trang đầu tiên (first = true)
                .andExpect(jsonPath("$.first").value(true))
                // Đây chưa phải là trang cuối cùng (last = false)
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    @DisplayName("Trang cuối chỉ còn 7 sản phẩm")
    void lastPage() throws Exception {
        // Gọi API và truyền thêm param trang số 1 (do bắt đầu từ 0 nên đây là trang thứ 2)
        mockMvc.perform(get(URL).param("page", "1").param("size", "10"))
                // Kết quả mong đợi 200 OK
                .andExpect(status().isOk())
                // Chỉ còn lại 7 sản phẩm thừa ra ở trang này
                .andExpect(jsonPath("$.content.length()").value(7))
                // Không phải trang đầu tiên nữa
                .andExpect(jsonPath("$.first").value(false))
                // Đây là trang cuối cùng (last = true)
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("Trang vượt quá tổng số trang -> content rỗng")
    void pageOutOfRange() throws Exception {
        // Gọi API nhưng đòi lấy tận trang số 50 (trong khi chỉ có 2 trang)
        mockMvc.perform(get(URL).param("page", "50"))
                // Hệ thống không báo lỗi (vì đây không phải là lỗi logic), chỉ trả về 200 OK
                .andExpect(status().isOk())
                // Mảng chứa sản phẩm (content) sẽ rỗng
                .andExpect(jsonPath("$.content.length()").value(0))
                // Tổng phần tử thực tế vẫn báo là 17 để frontend biết
                .andExpect(jsonPath("$.totalElements").value(17));
    }

    @Test
    @DisplayName("Lọc theo categoryId")
    void filterByCategory() throws Exception {
        // Lọc tất cả sản phẩm thuộc nhóm Electronics
        mockMvc.perform(get(URL).param("categoryId", String.valueOf(electronics.getId())))
                // Bắt buộc phải trả về đúng 12 sản phẩm Phone
                .andExpect(jsonPath("$.totalElements").value(12));
                
        // Lọc tất cả sản phẩm thuộc nhóm Books
        mockMvc.perform(get(URL).param("categoryId", String.valueOf(books.getId())))
                // Bắt buộc phải trả về đúng 5 sản phẩm Java Book
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("Tìm theo keyword không phân biệt hoa thường")
    void filterByKeyword() throws Exception {
        // Tìm chữ "phone" (viết thường)
        mockMvc.perform(get(URL).param("keyword", "phone"))
                // Phải ra 12 sản phẩm (vì trong tên có chứa "Phone")
                .andExpect(jsonPath("$.totalElements").value(12));
                
        // Tìm chữ "JAVA" (viết hoa toàn bộ)
        mockMvc.perform(get(URL).param("keyword", "JAVA"))
                // Phải ra 5 sản phẩm (vì trong tên chứa "Java")
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("Ký tự đặc biệt % trong keyword được coi là ký tự thường")
    void keywordWildcardIsEscaped() throws Exception {
        // Nếu người dùng cố tình nhập % (để hack chuỗi LIKE trong SQL)
        mockMvc.perform(get(URL).param("keyword", "%"))
                // Hệ thống phải coi đó là chữ "%" thực tế chứ không phải ký tự đại diện wildcard, nên trả ra 0 kết quả
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Lọc theo khoảng giá")
    void filterByPriceRange() throws Exception {
        // Lọc sản phẩm có giá từ 1000 trở lên
        mockMvc.perform(get(URL).param("minPrice", "1000"))
                // Gồm Phone 10 (1000), Phone 11 (1100), Phone 12 (1200) -> 3 sản phẩm
                .andExpect(jsonPath("$.totalElements").value(3));
                
        // Lọc sản phẩm có giá nhỏ hơn hoặc bằng 30
        mockMvc.perform(get(URL).param("maxPrice", "30"))
                // Gồm Java Book 1 (10), 2 (20), 3 (30) -> 3 sản phẩm
                .andExpect(jsonPath("$.totalElements").value(3));
                
        // Lọc khoảng từ 100 đến 300
        mockMvc.perform(get(URL).param("minPrice", "100").param("maxPrice", "300"))
                // Gồm Phone 01, 02, 03 -> 3 sản phẩm
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("Lọc còn hàng / hết hàng")
    void filterByStock() throws Exception {
        // Lọc chỉ các sản phẩm CÒN HÀNG (inStock = true)
        mockMvc.perform(get(URL).param("inStock", "true"))
                // Tổng 17 - 4 cái hết hàng = 13 sản phẩm
                .andExpect(jsonPath("$.totalElements").value(13));
                
        // Lọc chỉ các sản phẩm HẾT HÀNG (inStock = false)
        mockMvc.perform(get(URL).param("inStock", "false"))
                // Chỉ có Phone 03, 06, 09, 12 là hết hàng (4 sản phẩm)
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    @DisplayName("Kết hợp nhiều điều kiện lọc")
    void combinedFilters() throws Exception {
        // Truyền cùng lúc 4 điều kiện: Danh mục, Từ khoá, Giá tối thiểu, Tình trạng kho
        mockMvc.perform(get(URL)
                        .param("categoryId", String.valueOf(electronics.getId()))
                        .param("keyword", "phone")
                        .param("minPrice", "500")
                        .param("inStock", "true"))
                // Gồm Phone 05, 07, 08, 10, 11 (Còn Phone 06, 09, 12 bị loại vì hết hàng) -> Tổng cộng 5 sản phẩm
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("Sắp xếp theo giá giảm dần")
    void sortByPriceDesc() throws Exception {
        // Truyền yêu cầu sortBy theo "price", chiều "desc", lấy 3 phần tử đầu (size=3)
        mockMvc.perform(get(URL).param("sortBy", "price").param("direction", "desc").param("size", "3"))
                // Giá đắt nhất (1200) là Phone 12
                .andExpect(jsonPath("$.content[0].name").value("Phone 12"))
                // Thứ nhì (1100) là Phone 11
                .andExpect(jsonPath("$.content[1].name").value("Phone 11"))
                // Thứ ba (1000) là Phone 10
                .andExpect(jsonPath("$.content[2].name").value("Phone 10"));
    }

    @Test
    @DisplayName("Sắp xếp theo tên tăng dần trong một category")
    void sortByNameAsc() throws Exception {
        // Tìm sách, xếp theo "name", chiều "asc", lấy 2 phần tử
        mockMvc.perform(get(URL)
                        .param("categoryId", String.valueOf(books.getId()))
                        .param("sortBy", "name").param("direction", "asc").param("size", "2"))
                // Tên alphabet nhỏ nhất là Java Book 1
                .andExpect(jsonPath("$.content[0].name").value("Java Book 1"))
                // Thứ hai là Java Book 2
                .andExpect(jsonPath("$.content[1].name").value("Java Book 2"))
                // Do có tổng cộng 5 quyển, size là 2, nên chia thành 3 trang (2+2+1)
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("Không có kết quả -> content rỗng, không lỗi")
    void noResult() throws Exception {
        // Gõ một từ khoá tìm kiếm sai hoàn toàn
        mockMvc.perform(get(URL).param("keyword", "khong-ton-tai"))
                // HTTP vẫn trả về 200 OK (Vì việc không có kết quả không phải là lỗi hệ thống)
                .andExpect(status().isOk())
                // Mảng kết quả = rỗng
                .andExpect(jsonPath("$.content.length()").value(0))
                // Tổng số lượng phần tử = 0
                .andExpect(jsonPath("$.totalElements").value(0))
                // Tổng số trang = 0
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("Tham số không hợp lệ -> 400 {error}")
    void invalidParameters() throws Exception {
        // Các trường hợp truyền số liệu láo để test xem hệ thống Validation có chặn được không
        
        // Truyền size = 0 (size hợp lệ là 1..100)
        mockMvc.perform(get(URL).param("size", "0")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
                
        // Truyền size = 101 (Vượt max 100)
        mockMvc.perform(get(URL).param("size", "101")).andExpect(status().isBadRequest());
        
        // Truyền page âm (Page phải từ 0 trở lên)
        mockMvc.perform(get(URL).param("page", "-1")).andExpect(status().isBadRequest());
        
        // Truyền trường muốn sắp xếp không tồn tại/không cho phép (password)
        mockMvc.perform(get(URL).param("sortBy", "password")).andExpect(status().isBadRequest());
        
        // Truyền hướng sắp xếp tầm bậy (Chỉ cho phép asc hoặc desc)
        mockMvc.perform(get(URL).param("direction", "up")).andExpect(status().isBadRequest());
        
        // Truyền giá nhỏ nhất bị âm
        mockMvc.perform(get(URL).param("minPrice", "-1")).andExpect(status().isBadRequest());
        
        // Truyền giá tối thiểu LỚN HƠN giá tối đa (Vô lý logic)
        mockMvc.perform(get(URL).param("minPrice", "500").param("maxPrice", "100"))
                .andExpect(status().isBadRequest());
                
        // Truyền categoryId là chữ cái thay vì số
        mockMvc.perform(get(URL).param("categoryId", "abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("/products/filter không bị nhầm với /products/{id}")
    void routeDoesNotConflictWithGetById() throws Exception {
        // Route chính: Lấy danh sách toàn bộ sản phẩm không dùng filter (Trả về mảng thẳng)
        mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(17));
                
        // Route filter: Trả về đối tượng Page có bọc mảng ở trong trường "content"
        // (Kiểm tra xem Spring Boot có bị nhầm lẫn route "/filter" với "/{id}" hay không)
        mockMvc.perform(get(URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
