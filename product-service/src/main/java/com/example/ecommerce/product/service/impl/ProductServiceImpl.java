package com.example.ecommerce.product.service.impl;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.product.dto.request.ProductCreateRequest;
import com.example.ecommerce.product.dto.request.ProductUpdateRequest;
import com.example.ecommerce.product.dto.response.ProductResponse;
import com.example.ecommerce.product.entity.Category;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.mapper.ProductMapper;
import com.example.ecommerce.product.repository.CategoryRepository;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request, Long userId) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ResourceConflictException("Product with SKU '" + request.sku() + "' already exists.");
        }
        Product product = productMapper.toEntity(request);
        product.setIsActive(request.isActive() != null ? request.isActive() : Boolean.TRUE);
        product.setCategory(getCategoryById(request.categoryId()));
        product.setCreatedBy(userId);
        product.setModifiedBy(userId);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(getProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request, Long userId) {
        Product product = getProductById(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setIsActive(request.isActive());
        product.setImageUrl(request.imageUrl());
        product.setCategory(getCategoryById(request.categoryId()));
        product.setModifiedBy(userId);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    private Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
    }
}
