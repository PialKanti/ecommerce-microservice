package com.example.ecommerce.product.service;

import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.product.dto.request.ProductCreateRequest;
import com.example.ecommerce.product.dto.request.ProductUpdateRequest;
import com.example.ecommerce.product.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse create(ProductCreateRequest request, Long userId);

    ProductResponse getById(Long id);

    ProductResponse getByIdForAdmin(Long id);

    PaginatedResponse<ProductResponse> getAll(String search, Long categoryId,
                                              Double minPrice, Double maxPrice, Pageable pageable);

    PaginatedResponse<ProductResponse> getAllForAdmin(String search, Long categoryId,
                                                      Double minPrice, Double maxPrice, Boolean isActive, Pageable pageable);

    ProductResponse update(Long id, ProductUpdateRequest request, Long userId);

    void delete(Long id);
}
