package com.fpt.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.category.entity.Category;
import com.fpt.category.exception.GlobalExceptionHandler;
import com.fpt.category.exception.ResourceNotFoundException;
import com.fpt.category.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("GET /api/v1/categories - Lấy tất cả category")
    void getAllCategories() throws Exception {
        Category c1 = new Category(); c1.setId(1L); c1.setName("C1");
        Category c2 = new Category(); c2.setId(2L); c2.setName("C2");

        when(categoryService.findAll()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("C1"))
                .andExpect(jsonPath("$[1].name").value("C2"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Lấy category theo ID thành công")
    void getCategoryByIdSuccess() throws Exception {
        Category c1 = new Category(); c1.setId(1L); c1.setName("C1");
        when(categoryService.findById(1L)).thenReturn(c1);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("C1"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Lỗi không tìm thấy category")
    void getCategoryByIdNotFound() throws Exception {
        when(categoryService.findById(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/categories/99"))
                .andExpect(status().isNotFound()); // Đảm bảo trả về 404
    }

    @Test
    @DisplayName("POST /api/v1/categories - Tạo category thành công")
    void createCategorySuccess() throws Exception {
        Category input = new Category(); input.setName("New");
        Category saved = new Category(); saved.setId(1L); saved.setName("New");

        when(categoryService.create(any(Category.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Lỗi trùng tên category")
    void createCategoryDuplicate() throws Exception {
        Category input = new Category(); input.setName("Dup");

        when(categoryService.create(any(Category.class))).thenThrow(new IllegalArgumentException("Duplicate"));

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest()); // Đảm bảo trả về 400
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Cập nhật thành công")
    void updateCategorySuccess() throws Exception {
        Category input = new Category(); input.setName("Updated");
        Category saved = new Category(); saved.setId(1L); saved.setName("Updated");

        when(categoryService.update(eq(1L), any(Category.class))).thenReturn(saved);

        mockMvc.perform(put("/api/v1/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Xóa thành công")
    void deleteCategorySuccess() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent()); // Trả về 204 No Content

        verify(categoryService).delete(1L);
    }
}
