package com.fpt.ecommerce.service;

import com.fpt.ecommerce.entity.Category;
import com.fpt.ecommerce.exception.ResourceNotFoundException;
import com.fpt.ecommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id = " + id));
    }

    @Transactional
    public Category create(Category category) {
        String name = category.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category '" + name + "' already exists");
        }
        category.setId(null);
        category.setName(name);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, Category input) {
        Category existing = findById(id);
        String name = input.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Category '" + name + "' already exists");
        }
        existing.setName(name);
        return categoryRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(findById(id));
    }
}