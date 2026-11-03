package com.fpt.ecommerce.service;

import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.exception.ResourceNotFoundException;
import com.fpt.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

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
    void findAll_ShouldReturnCategoryList() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenCategoryExists_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        Category result = categoryService.findById(1L);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(99L));
        verify(categoryRepository, times(1)).findById(99L);
    }

    @Test
    void create_WhenNameIsUnique_ShouldSaveAndReturnCategory() {
        Category input = new Category();
        input.setName(" Toys "); // Test trimming
        
        when(categoryRepository.existsByNameIgnoreCase("Toys")).thenReturn(false);
        
        Category savedCategory = new Category();
        savedCategory.setId(3L);
        savedCategory.setName("Toys");
        
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        Category result = categoryService.create(input);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Toys", result.getName());
        verify(categoryRepository, times(1)).existsByNameIgnoreCase("Toys");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void create_WhenNameAlreadyExists_ShouldThrowIllegalArgumentException() {
        Category input = new Category();
        input.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.create(input));
        verify(categoryRepository, times(1)).existsByNameIgnoreCase("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void update_WhenCategoryExistsAndNameIsUnique_ShouldUpdateAndReturnCategory() {
        Category input = new Category();
        input.setName(" Updated Electronics ");
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Updated Electronics", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category1);

        Category result = categoryService.update(1L, input);

        assertEquals("Updated Electronics", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByNameIgnoreCaseAndIdNot("Updated Electronics", 1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void update_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        Category input = new Category();
        input.setName("Books");

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(99L, input));
        verify(categoryRepository, times(1)).findById(99L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void update_WhenNameAlreadyExistsForOtherCategory_ShouldThrowIllegalArgumentException() {
        Category input = new Category();
        input.setName("Books"); // Try to rename id 1 to "Books" (which exists)

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Books", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.update(1L, input));
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByNameIgnoreCaseAndIdNot("Books", 1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void delete_WhenCategoryExists_ShouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        doNothing().when(categoryRepository).delete(category1);

        categoryService.delete(1L);

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).delete(category1);
    }

    @Test
    void delete_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(99L));
        verify(categoryRepository, times(1)).findById(99L);
        verify(categoryRepository, never()).delete(any());
    }
}
