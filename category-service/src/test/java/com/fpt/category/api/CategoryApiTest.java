package com.fpt.category.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.category.client.ProductClient;
import com.fpt.category.entity.Category;
import com.fpt.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
class CategoryApiTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private ProductClient productClient; // mặc định trả 0 sản phẩm

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        categoryRepository.deleteAll(); // Làm sạch DB trước mỗi test để đảm bảo độc lập
    }

    @Test
    @DisplayName("Full API Flow: Tạo, lấy ds, cập nhật, xóa Category với DB thật")
    void createAndGetCategoriesFlow() throws Exception {
        // 1. Tạo Category mới
        Category newCategory = new Category();
        newCategory.setName("Laptops");

        String responseBody = mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Laptops")))
                .andReturn().getResponse().getContentAsString();

        Category savedCategory = objectMapper.readValue(responseBody, Category.class);
        Long id = savedCategory.getId();

        // 2. Lấy danh sách Categories để xác nhận có 1 category vừa tạo
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Laptops")));

        // 3. Lấy Category theo ID
        mockMvc.perform(get("/api/v1/categories/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Laptops")));

        // 4. Update Category
        Category updateCategory = new Category();
        updateCategory.setName("Gaming Laptops");

        mockMvc.perform(put("/api/v1/categories/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Gaming Laptops")));

        // 5. Delete Category
        mockMvc.perform(delete("/api/v1/categories/" + id))
                .andExpect(status().isNoContent());

        // 6. Kiểm tra lại Category đã bị xóa (phải trả về 404)
        mockMvc.perform(get("/api/v1/categories/" + id))
                .andExpect(status().isNotFound());
        
        // 7. Verify trong Database thật
        assertFalse(categoryRepository.existsById(id));
    }

    @Test
    @DisplayName("API lỗi 409 khi xóa category vẫn còn sản phẩm, category được giữ lại")
    void deleteCategoryWithProductsReturnsConflict() throws Exception {
        Category category = new Category();
        category.setName("Tablets");
        Long id = categoryRepository.save(category).getId();
        when(productClient.countByCategory(id)).thenReturn(2L);

        mockMvc.perform(delete("/api/v1/categories/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("2 product")));

        assertTrue(categoryRepository.existsById(id));
    }

    @Test
    @DisplayName("API lỗi 400 khi tạo category bị trùng tên")
    void createDuplicateCategoryThrowsError() throws Exception {
        // Tạo category lần 1
        Category category = new Category();
        category.setName("Smartphones");

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated());

        // Tạo category lần 2 với cùng tên sẽ dính lỗi Validation/IllegalArgument
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already exists")));
    }
}
