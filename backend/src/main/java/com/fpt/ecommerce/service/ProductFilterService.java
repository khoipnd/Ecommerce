package com.fpt.ecommerce.service;

import com.fpt.ecommerce.dto.PageResponse;
import com.fpt.ecommerce.dto.ProductFilterCriteria;
import com.fpt.ecommerce.entity.Product;
import com.fpt.ecommerce.repository.ProductFilterRepository;
import com.fpt.ecommerce.specification.ProductSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Lọc + phân trang + sắp xếp sản phẩm (BE-10, BE-11).
 * Dữ liệu đầu vào sai sẽ ném IllegalArgumentException -> GlobalExceptionHandler trả 400.
 */
@Service
@Transactional(readOnly = true)
public class ProductFilterService {

    public static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "price", "stockQuantity");

    private final ProductFilterRepository productFilterRepository;

    public ProductFilterService(ProductFilterRepository productFilterRepository) {
        this.productFilterRepository = productFilterRepository;
    }

    public PageResponse<Product> filter(ProductFilterCriteria criteria) {
        String sortBy = normalizeSortBy(criteria.sortBy());
        Sort.Direction direction = parseDirection(criteria.direction());
        validate(criteria);

        Sort sort = Sort.by(direction, sortBy);
        if (!"id".equals(sortBy)) {
            // sort phụ theo id để thứ tự ổn định giữa các trang
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        String keyword = criteria.keyword() == null ? null : criteria.keyword().trim();

        Specification<Product> spec = ProductSpecifications.matchAll()
                .and(ProductSpecifications.hasCategory(criteria.categoryId()))
                .and(ProductSpecifications.nameContains(keyword))
                .and(ProductSpecifications.priceAtLeast(criteria.minPrice()))
                .and(ProductSpecifications.priceAtMost(criteria.maxPrice()))
                .and(ProductSpecifications.inStock(criteria.inStock()));

        return PageResponse.from(productFilterRepository.findAll(spec, pageable));
    }

    private void validate(ProductFilterCriteria c) {
        if (c.page() < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (c.size() < 1 || c.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (c.minPrice() != null && c.minPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("minPrice must be greater than or equal to 0");
        }
        if (c.maxPrice() != null && c.maxPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("maxPrice must be greater than or equal to 0");
        }
        if (c.minPrice() != null && c.maxPrice() != null && c.minPrice().compareTo(c.maxPrice()) > 0) {
            throw new IllegalArgumentException("minPrice must not be greater than maxPrice");
        }
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }
        String value = sortBy.trim();
        if (!SORTABLE_FIELDS.contains(value)) {
            throw new IllegalArgumentException(
                    "sortBy must be one of: id, name, price, stockQuantity");
        }
        return value;
    }

    private Sort.Direction parseDirection(String direction) {
        if (direction == null || direction.isBlank() || "asc".equalsIgnoreCase(direction.trim())) {
            return Sort.Direction.ASC;
        }
        if ("desc".equalsIgnoreCase(direction.trim())) {
            return Sort.Direction.DESC;
        }
        throw new IllegalArgumentException("direction must be 'asc' or 'desc'");
    }
}
