package com.example.ecommerce.product.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.product.dto.response.ProductResponse;
import com.example.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping(ApiEndpoints.Product.BASE_PRODUCTS)
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public read-only access to the active product catalog. No authentication required.")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get product by ID",
            description = "Returns a single active product by its numeric ID. Returns 404 if the product does not exist or is inactive."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found or inactive", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @Parameter(description = "Numeric ID of the product", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @Operation(
            summary = "List active products",
            description = "Returns a paginated list of active products. Supports optional search (partial match on name or SKU, " +
                    "case-insensitive) and filtering by category, minimum price, and maximum price."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page of products returned successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> listProducts(
            @Parameter(description = "Partial match on product name or SKU (case-insensitive)", example = "iphone")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum price (inclusive)", example = "100.00")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price (inclusive)", example = "500.00")
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getAll(search, categoryId, minPrice, maxPrice, PageRequest.of(page, size))));
    }
}
