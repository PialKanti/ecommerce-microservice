package com.example.ecommerce.product.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.product.dto.response.CategoryResponse;
import com.example.ecommerce.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Category.BASE_CATEGORIES)
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Public product category browsing")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getById(id)));
    }

    @Operation(summary = "List all categories (paginated)")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<CategoryResponse>>> listCategories(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.getAll(PageRequest.of(page, size))));
    }
}
