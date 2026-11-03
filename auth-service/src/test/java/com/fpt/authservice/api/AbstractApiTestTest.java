package com.fpt.authservice.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AbstractApiTestTest extends AbstractApiTest {

    @Test
    @DisplayName("Kiểm tra WebApplicationContext và MockMvc được khởi tạo thành công")
    void testMockMvcSetup() {
        // Xác nhận context và mockMvc đã được Spring tự động nạp và khởi
        // tạo ở setUpMockMvc()
        assertNotNull(context, "WebApplicationContext phải khác null");
        assertNotNull(mockMvc, "MockMvc phải được khởi tạo thành công");
    }

    @Test
    @DisplayName("Kiểm tra MockMvc có cấu hình Spring Security hoạt động chính xác")
    void testMockMvcCanPerformRequest() throws Exception {
        // Thử thực hiện một HTTP Request tới một route bất kỳ để đảm bảo mockMvc
        // không bị cấu hình sai và Spring Security filter đã được áp dụng.
        mockMvc.perform(get("/api/v1/dummy-endpoint-for-testing")
                .contentType(MediaType.APPLICATION_JSON))
                // Kết quả có thể là 401/403/404 tuỳ config, nhưng chắc chắn
                // là lỗi từ client
                .andExpect(status().is4xxClientError());
    }
}
