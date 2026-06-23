package com.example.ecommerce.auth.repository;

import com.example.ecommerce.auth.entity.Role;
import com.example.ecommerce.commons.enums.RoleCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByCode(RoleCode code);

    Optional<Role> findByCode(RoleCode code);

    @EntityGraph(attributePaths = "permissions")
    @Query("select role from Role role where role.id = :id")
    Optional<Role> findWithPermissionsById(@Param("id") Long id);
}
