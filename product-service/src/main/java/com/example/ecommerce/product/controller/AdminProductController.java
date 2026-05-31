package com.example.ecommerce.product.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.product.dto.request.ProductCreateRequest;
import com.example.ecommerce.product.dto.request.ProductUpdateRequest;
import com.example.ecommerce.product.dto.response.ProductResponse;
import com.example.ecommerce.product.service.ProductService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_PRODUCTS)
@RequiredArgsConstructor
@Tag(name = "Admin Products", description = "Administrative product catalog mutations")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductService productService;

    @Operation(
            summary = "Create product",
            description = "Requires ROLE_ADMIN or ROLE_PRODUCT_MANAGER with PERMISSION_PRODUCT_CREATE.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "sku": "PHONE-001",
                                      "name": "Smartphone",
                                      "description": "Android smartphone with 128GB storage.",
                                      "price": 499.99,
                                      "isActive": true,
                                      "categoryId": 1,
                                      "imageUrl": "https://example.com/products/phone-001.jpg"
                                    }
                                    """)
                    )
            )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Product created successfully",
                productService.create(request, userId)));
    }

    @Operation(summary = "Update product",
            description = "Requires ROLE_ADMIN or ROLE_PRODUCT_MANAGER with PERMISSION_PRODUCT_UPDATE.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully",
                productService.update(id, request, userId)));
    }

    @Operation(summary = "Delete product",
            description = "Requires ROLE_ADMIN or ROLE_PRODUCT_MANAGER with PERMISSION_PRODUCT_DELETE.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
