package com.example.ecommerce.product.service;

import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.product.dto.request.CategoryCreateRequest;
import com.example.ecommerce.product.dto.request.CategoryUpdateRequest;
import com.example.ecommerce.product.dto.response.CategoryResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponse create(CategoryCreateRequest request, Long userId);

    CategoryResponse getById(Long id);

    PaginatedResponse<CategoryResponse> getAll(Pageable pageable);

    CategoryResponse update(Long id, CategoryUpdateRequest request, Long userId);

    CategoryResponse toggleStatus(Long id, Boolean isActive, Long userId);

    void delete(Long id);
}
