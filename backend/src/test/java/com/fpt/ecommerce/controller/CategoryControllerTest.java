package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

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
    void getAll_ShouldReturnCategoryList() {
        when(categoryService.findAll()).thenReturn(Arrays.asList(category1, category2));

        List<Category> result = categoryController.getAll();

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(categoryService, times(1)).findAll();
    }

    @Test
    void getById_ShouldReturnCategory() {
        when(categoryService.findById(1L)).thenReturn(category1);

        Category result = categoryController.getById(1L);

        assertEquals("Electronics", result.getName());
        verify(categoryService, times(1)).findById(1L);
    }

    @Test
    void create_ShouldReturnCreatedCategory() {
        when(categoryService.create(any(Category.class))).thenReturn(category1);

        ResponseEntity<Category> response = categoryController.create(category1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Electronics", response.getBody().getName());
        verify(categoryService, times(1)).create(category1);
    }

    @Test
    void update_ShouldReturnUpdatedCategory() {
        when(categoryService.update(eq(1L), any(Category.class))).thenReturn(category1);

        Category result = categoryController.update(1L, category1);

        assertEquals("Electronics", result.getName());
        verify(categoryService, times(1)).update(1L, category1);
    }

    @Test
    void delete_ShouldCallServiceDelete() {
        doNothing().when(categoryService).delete(1L);

        categoryController.delete(1L);

        verify(categoryService, times(1)).delete(1L);
    }
}
