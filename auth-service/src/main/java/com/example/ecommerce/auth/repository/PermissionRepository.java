package com.example.ecommerce.auth.repository;

import com.example.ecommerce.auth.entity.Permission;
import com.example.ecommerce.commons.enums.PermissionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByCode(PermissionCode code);

    Optional<Permission> findByCode(PermissionCode code);
}
