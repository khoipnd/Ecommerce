package com.fpt.ecommerce.service;

// Import DTO và Entity
import com.fpt.ecommerce.dto.ProductFilterCriteria;
import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.dto.PageResponse;
// Import Repository
import com.fpt.ecommerce.repository.ProductFilterRepository;
// JUnit 5 Annotations
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

// Import các hàm kiểm tra kết quả (Assert)
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// Import các hàm giả lập của Mockito
import static org.mockito.Mockito.*;

// Spring Data JPA Pagination & Specification
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Unit test thuần (không cần Spring Context/Database): kiểm tra logic bắt lỗi (validate) 
 * đầu vào của ProductFilterService.
 * Vì logic validate (bắt lỗi size, page...) được chạy ngay ở đầu hàm, trước cả khi gọi Repository, 
 * nên ở một số test case, ta có thể khởi tạo Service và truyền repository = null mà không sợ lỗi NullPointerException.
 */
class ProductFilterServiceTest {

    // [ARRANGE CẤP ĐỘ CLASS]
    // Khởi tạo một đối tượng Service thực tế, truyền thẳng giá trị null cho Repository.
    // Việc này hoàn toàn an toàn đối với các test case chuyên bắt lỗi Validation vì code sẽ bị văng exception 
    // trước khi chạy tới dòng gọi Repository.
    private final ProductFilterService service = new ProductFilterService(null);

    // Hàm hỗ trợ (helper): Khởi tạo nhanh một đối tượng tiêu chí lọc (Criteria) để tránh code lặp lại
    private ProductFilterCriteria criteria(int page, int size, String sortBy, String direction,
                                           BigDecimal min, BigDecimal max) {
        return new ProductFilterCriteria(null, null, min, max, null, page, size, sortBy, direction);
    }

    // Hàm hỗ trợ: Nhận vào tiêu chí sai, chạy hàm filter, và xác nhận rằng hệ thống sẽ ném ra lỗi IllegalArgumentException
    // Đồng thời kiểm tra xem trong câu thông báo lỗi có chứa từ khoá (tên biến) mong đợi hay không
    private void assertBadRequest(ProductFilterCriteria criteria, String expectedMessagePart) {
        // [ACT & ASSERT]
        // Bắt lỗi khi gọi hàm filter
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.filter(criteria));
        
        // Kiểm tra trong nội dung câu lỗi có chứa chữ chỉ điểm (ví dụ: "page", "size") để đảm bảo bắt đúng lỗi
        assertTrue(ex.getMessage().contains(expectedMessagePart),
                "Message was: " + ex.getMessage());
    }

    /**
     * Kiểm tra nếu truyền số trang (page) âm thì sẽ bị từ chối.
     */
    @Test
    @DisplayName("page âm bị từ chối")
    void negativePage() {
        // [ACT & ASSERT]
        // Gọi hàm helper: Truyền page = -1 (sai) -> Bắt buộc ném lỗi chứa chữ "page"
        assertBadRequest(criteria(-1, 10, "id", "asc", null, null), "page");
    }

    /**
     * Kiểm tra nếu kích thước trang (size) nhỏ hơn 1 hoặc lớn hơn 100 thì bị từ chối.
     */
    @Test
    @DisplayName("size ngoài khoảng 1..100 bị từ chối")
    void sizeOutOfRange() {
        // [ACT & ASSERT]
        // Size = 0 -> Sai -> Phải chứa thông báo lỗi chữ "size"
        assertBadRequest(criteria(0, 0, "id", "asc", null, null), "size");
        
        // Size = 101 (vượt MAX = 100) -> Sai -> Phải có thông báo lỗi "size"
        assertBadRequest(criteria(0, ProductFilterService.MAX_PAGE_SIZE + 1, "id", "asc", null, null), "size");
    }

    /**
     * Kiểm tra nếu trường cần sắp xếp (sortBy) không nằm trong danh sách cho phép (whitelist) thì bị từ chối.
     */
    @Test
    @DisplayName("sortBy ngoài whitelist bị từ chối")
    void invalidSortBy() {
        // [ACT & ASSERT]
        // Sort theo trường "category" (không nằm trong danh sách cho phép như id, name, price, v.v.) -> Báo lỗi "sortBy"
        assertBadRequest(criteria(0, 10, "category", "asc", null, null), "sortBy");
    }

    /**
     * Kiểm tra nếu hướng sắp xếp (direction) khác asc (tăng dần) hoặc desc (giảm dần) thì bị từ chối.
     */
    @Test
    @DisplayName("direction khác asc/desc bị từ chối")
    void invalidDirection() {
        // [ACT & ASSERT]
        // Sắp xếp theo chiều "sideways" (tầm bậy) -> Báo lỗi "direction"
        assertBadRequest(criteria(0, 10, "id", "sideways", null, null), "direction");
    }

    /**
     * Kiểm tra nếu khoảng giá lọc không hợp lệ (âm hoặc min > max) thì bị từ chối.
     */
    @Test
    @DisplayName("minPrice/maxPrice âm hoặc min > max bị từ chối")
    void invalidPriceRange() {
        // [ACT & ASSERT]
        // Giá nhỏ nhất bị âm (-1) -> Lỗi "minPrice"
        assertBadRequest(criteria(0, 10, "id", "asc", new BigDecimal("-1"), null), "minPrice");
        
        // Giá lớn nhất bị âm (-1) -> Lỗi "maxPrice"
        assertBadRequest(criteria(0, 10, "id", "asc", null, new BigDecimal("-1")), "maxPrice");
        
        // Giá nhỏ nhất (10) lại LỚN HƠN giá lớn nhất (5) -> Vô lý -> Lỗi "minPrice"
        assertBadRequest(criteria(0, 10, "id", "asc", new BigDecimal("10"), new BigDecimal("5")), "minPrice");
    }

    /**
     * Kiểm tra phương thức filter gọi đúng đến repository phân trang khi các điều kiện lọc hợp lệ.
     */
    @Test
    @DisplayName("filter valid criteria should call repository")
    void filterValid() {
        // [ARRANGE - CHUẨN BỊ]
        // Vì đây là test case "luồng đúng" (hợp lệ), code SẼ GỌI tới Repository. 
        // Do đó ta không thể dùng đối tượng service(null) ở trên được, mà phải tự tạo một bản mock của Repository.
        ProductFilterRepository repo = mock(ProductFilterRepository.class);
        
        // Nhồi mock Repository đó vào trong Service
        ProductFilterService mockService = new ProductFilterService(repo);
        
        // Chuẩn bị tiêu chí hoàn toàn hợp lệ
        ProductFilterCriteria validCriteria = criteria(0, 10, "id", "asc", null, null);

        // Cấu hình mock: Khi service gọi repo.findAll(Specification, Pageable), hãy trả về một trang kết quả ảo (chứa 1 sản phẩm)
        when(repo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Product())));

        // [ACT - THỰC THI]
        // Gọi hàm lọc với tiêu chí đúng
        PageResponse<Product> response = mockService.filter(validCriteria);
        
        // [ASSERT - KIỂM TRA]
        // Kết quả trả về không được rỗng
        assertNotNull(response);
        
        // Xác minh (Verify) cực kỳ quan trọng: Đảm bảo rằng hàm findAll() của Repository THỰC SỰ ĐÃ ĐƯỢC GỌI đúng 1 lần.
        // Điều này chứng minh rằng logic Validation đã vượt qua thành công và chuyển lệnh xuống DB.
        verify(repo, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
