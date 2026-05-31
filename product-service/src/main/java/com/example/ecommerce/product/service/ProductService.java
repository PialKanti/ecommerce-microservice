package com.example.ecommerce.product.service;

import com.example.ecommerce.product.dto.request.ProductCreateRequest;
import com.example.ecommerce.product.dto.request.ProductUpdateRequest;
import com.example.ecommerce.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse create(ProductCreateRequest request, Long userId);

    ProductResponse getById(Long id);

    Page<ProductResponse> getAll(Pageable pageable);

    ProductResponse update(Long id, ProductUpdateRequest request, Long userId);

    void delete(Long id);
}
