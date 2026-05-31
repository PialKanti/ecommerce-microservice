package com.example.ecommerce.product.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.product.dto.request.ProductCreateRequest;
import com.example.ecommerce.product.dto.request.ProductUpdateRequest;
import com.example.ecommerce.product.dto.response.ProductResponse;
import com.example.ecommerce.product.service.ProductService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_PRODUCTS)
@RequiredArgsConstructor
@Tag(name = "Admin — Products",
        description = "Write operations for the product catalog. Requires `ROLE_ADMIN` or `ROLE_PRODUCT_MANAGER` " +
                "with the matching permission, enforced by the api-gateway.")
public class AdminProductController {

    private final ProductService productService;

    @Operation(
            summary = "Create a product",
            description = "Creates a new product and assigns it to an existing category. Required permission: `PRODUCT_CREATE`. " +
                    "The caller's identity is supplied by the api-gateway via the `X-User-Id` header.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "sku": "PHONE-001",
                                      "name": "Smartphone Pro 128GB",
                                      "description": "Android smartphone with 128 GB internal storage.",
                                      "price": 499.99,
                                      "isActive": true,
                                      "categoryId": 1,
                                      "imageUrl": "https://cdn.example.com/products/phone-001.jpg"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error — check request body fields", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "A product with that SKU already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Product created successfully",
                productService.create(request, userId)));
    }

    @Operation(
            summary = "Update a product",
            description = "Updates the details of an existing product. `sku` is immutable and cannot be changed. Required permission: `PRODUCT_UPDATE`.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Smartphone Pro 256GB",
                                      "description": "Android smartphone with 256 GB internal storage.",
                                      "price": 599.99,
                                      "isActive": true,
                                      "categoryId": 1,
                                      "imageUrl": "https://cdn.example.com/products/phone-001-256.jpg"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error — check request body fields", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "Numeric ID of the product to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully",
                productService.update(id, request, userId)));
    }

    @Operation(
            summary = "Delete a product",
            description = "Permanently deletes a product. Required permission: `PRODUCT_DELETE`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Numeric ID of the product to delete", example = "1", required = true)
            @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
