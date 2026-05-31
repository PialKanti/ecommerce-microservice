package com.example.ecommerce.product.mapper;

import com.example.ecommerce.product.dto.request.ProductCreateRequest;
import com.example.ecommerce.product.dto.response.ProductResponse;
import com.example.ecommerce.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductCreateRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);
}
