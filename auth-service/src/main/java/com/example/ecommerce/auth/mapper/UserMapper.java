package com.example.ecommerce.auth.mapper;

import com.example.ecommerce.auth.dto.request.RegisterRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.entity.Role;
import com.example.ecommerce.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "roles", expression = "java(toRoleCodes(user))")
    UserResponse toResponse(User user);

    default Set<String> toRoleCodes(User user) {
        return user.getRoles()
                .stream()
                .map(Role::getCode)
                .map(Enum::name)
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }
}
