package com.example.ecommerce.product.service.impl;

import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.product.dto.request.CategoryCreateRequest;
import com.example.ecommerce.product.dto.request.CategoryUpdateRequest;
import com.example.ecommerce.product.dto.response.CategoryResponse;
import com.example.ecommerce.product.entity.Category;
import com.example.ecommerce.product.mapper.CategoryMapper;
import com.example.ecommerce.product.repository.CategoryRepository;
import com.example.ecommerce.product.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryCreateRequest request, Long userId) {
        if (categoryRepository.existsByCode(request.code())) {
            throw new ResourceConflictException("Category with code '" + request.code() + "' already exists.");
        }
        Category category = categoryMapper.toEntity(request);
        category.setCreatedBy(userId);
        category.setModifiedBy(userId);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(getCategoryById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> getAll(Pageable pageable) {
        return PaginatedResponse.of(categoryRepository.findAll(pageable).map(categoryMapper::toResponse));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request, Long userId) {
        Category category = getCategoryById(id);
        category.setName(request.name());
        category.setDescription(request.description());
        category.setModifiedBy(userId);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse toggleStatus(Long id, Boolean isActive, Long userId) {
        Category category = getCategoryById(id);
        category.setIsActive(isActive);
        category.setModifiedBy(userId);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }
}
