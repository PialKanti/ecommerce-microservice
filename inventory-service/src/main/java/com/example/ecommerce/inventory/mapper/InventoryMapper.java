package com.example.ecommerce.inventory.mapper;

import com.example.ecommerce.inventory.dto.request.InventoryCreateRequest;
import com.example.ecommerce.inventory.dto.response.InventoryResponse;
import com.example.ecommerce.inventory.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    Inventory toEntity(InventoryCreateRequest request);

    @Mapping(
            target = "availableQuantity",
            expression = "java(inventory.getTotalQuantity() - inventory.getReservedQuantity())"
    )
    InventoryResponse toResponse(Inventory inventory);
}
