package com.example.ecommerce.product.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.product.dto.request.CategoryCreateRequest;
import com.example.ecommerce.product.dto.request.CategoryUpdateRequest;
import com.example.ecommerce.product.dto.response.CategoryResponse;
import com.example.ecommerce.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_CATEGORIES)
@RequiredArgsConstructor
@Tag(name = "Admin Categories", description = "Administrative product category mutations")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Create category",
            description = "Requires ROLE_ADMIN or ROLE_PRODUCT_MANAGER with PERMISSION_CATEGORY_CREATE.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Electronics",
                                      "code": "ELEC",
                                      "description": "Devices and gadgets."
                                    }
                                    """)
                    )
            )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Category created successfully",
                categoryService.create(request, userId)));
    }

    @Operation(summary = "Update category",
            description = "Requires ROLE_ADMIN or ROLE_PRODUCT_MANAGER with PERMISSION_CATEGORY_UPDATE.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully",
                categoryService.update(id, request, userId)));
    }

    @Operation(summary = "Toggle category active status",
            description = "Requires ROLE_ADMIN or ROLE_PRODUCT_MANAGER with PERMISSION_CATEGORY_UPDATE.")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggleStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.toggleStatus(id, isActive, userId)));
    }

    @Operation(summary = "Delete category",
            description = "Requires ROLE_ADMIN or ROLE_PRODUCT_MANAGER with PERMISSION_CATEGORY_DELETE.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
