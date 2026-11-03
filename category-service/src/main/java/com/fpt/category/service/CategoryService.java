package com.fpt.category.service;

import com.fpt.category.client.ProductClient;
import com.fpt.category.entity.Category;
import com.fpt.category.exception.CategoryInUseException;
import com.fpt.category.exception.ResourceNotFoundException;
import com.fpt.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductClient productClient;

    public CategoryService(CategoryRepository categoryRepository, ProductClient productClient) {
        this.categoryRepository = categoryRepository;
        this.productClient = productClient;
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
        Category category = findById(id);
        // Product nằm ở DB khác nên không có khoá ngoại chặn, phải hỏi product-service
        long productCount = productClient.countByCategory(id);
        if (productCount > 0) {
            throw new CategoryInUseException("Category '" + category.getName() + "' still has "
                    + productCount + " product(s); move or delete them first");
        }
        categoryRepository.delete(category);
    }
}