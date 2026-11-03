package com.fpt.category.service;

import com.fpt.category.client.ProductClient;
import com.fpt.category.entity.Category;
import com.fpt.category.exception.CategoryInUseException;
import com.fpt.category.exception.ResourceNotFoundException;
import com.fpt.category.exception.ServiceUnavailableException;
import com.fpt.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Books");
    }

    @Test
    @DisplayName("Lấy tất cả category")
    void findAllSuccess() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        List<Category> categories = categoryService.findAll();
        assertEquals(2, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }

    @Test
    @DisplayName("Tìm category theo id thành công")
    void findByIdSuccess() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        Category found = categoryService.findById(1L);
        assertEquals("Electronics", found.getName());
    }

    @Test
    @DisplayName("Tìm category theo id không tồn tại ném ResourceNotFoundException")
    void findByIdNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
    }

    @Test
    @DisplayName("Tạo category thành công")
    void createSuccess() {
        Category input = new Category();
        input.setName("  New Category  ");

        when(categoryRepository.existsByNameIgnoreCase("New Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category saved = i.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        Category result = categoryService.create(input);

        assertNotNull(result.getId());
        assertEquals("New Category", result.getName()); // đảm bảo đã được trim()
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Tạo category thất bại do trùng tên")
    void createFailDuplicateName() {
        Category input = new Category();
        input.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.create(input));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Cập nhật category thành công")
    void updateSuccess() {
        Category input = new Category();
        input.setName("Updated Electronics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Updated Electronics", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category1);

        Category result = categoryService.update(1L, input);

        assertEquals("Updated Electronics", result.getName());
        verify(categoryRepository).save(category1);
    }

    @Test
    @DisplayName("Cập nhật category thất bại do trùng tên với category khác")
    void updateFailDuplicateName() {
        Category input = new Category();
        input.setName("Books"); // Same as category2

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Books", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.update(1L, input));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Xóa category thành công")
    void deleteSuccess() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(productClient.countByCategory(1L)).thenReturn(0L);

        categoryService.delete(1L);

        verify(categoryRepository).delete(category1);
    }

    @Test
    @DisplayName("Xóa category thất bại khi vẫn còn sản phẩm")
    void deleteFailWhenCategoryHasProducts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(productClient.countByCategory(1L)).thenReturn(3L);

        CategoryInUseException ex = assertThrows(CategoryInUseException.class, () -> categoryService.delete(1L));
        assertTrue(ex.getMessage().contains("3 product"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Không xóa category khi product-service không phản hồi")
    void deleteFailWhenProductServiceUnavailable() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(productClient.countByCategory(1L)).thenThrow(new ServiceUnavailableException("down"));

        assertThrows(ServiceUnavailableException.class, () -> categoryService.delete(1L));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Xóa category không tồn tại không gọi product-service")
    void deleteNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(99L));
        verifyNoInteractions(productClient);
    }
}
