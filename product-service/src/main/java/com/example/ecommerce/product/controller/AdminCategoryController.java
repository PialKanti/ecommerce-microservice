package com.example.ecommerce.product.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.product.dto.request.CategoryCreateRequest;
import com.example.ecommerce.product.dto.request.CategoryUpdateRequest;
import com.example.ecommerce.product.dto.response.CategoryResponse;
import com.example.ecommerce.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Admin — Categories",
        description = "Write operations for product categories. Requires `ROLE_ADMIN` or `ROLE_PRODUCT_MANAGER` " +
                "with the matching permission, enforced by the api-gateway.")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Create a category",
            description = "Creates a new product category. Required permission: `CATEGORY_CREATE`. " +
                    "The caller's identity is supplied by the api-gateway via the `X-User-Id` header.",
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error — check request body fields", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "A category with that code already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Category created successfully",
                categoryService.create(request, userId)));
    }

    @Operation(
            summary = "Update a category",
            description = "Updates the name and description of an existing category. Required permission: `CATEGORY_UPDATE`.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Consumer Electronics",
                                      "description": "All consumer electronic devices and accessories."
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error — check request body fields", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Numeric ID of the category to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully",
                categoryService.update(id, request, userId)));
    }

    @Operation(
            summary = "Toggle category active status",
            description = "Activates or deactivates a category. This operation is idempotent. Required permission: `CATEGORY_UPDATE`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggleStatus(
            @Parameter(description = "Numeric ID of the category", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Pass `true` to activate, `false` to deactivate", example = "false", required = true)
            @RequestParam Boolean isActive,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.toggleStatus(id, isActive, userId)));
    }

    @Operation(
            summary = "Delete a category",
            description = "Permanently deletes a category. Required permission: `CATEGORY_DELETE`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Numeric ID of the category to delete", example = "1", required = true)
            @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
