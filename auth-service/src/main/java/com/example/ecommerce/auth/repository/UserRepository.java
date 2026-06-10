package com.example.ecommerce.auth.repository;

import com.example.ecommerce.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select user from User user where user.id = :id")
    Optional<User> findWithRolesById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"roles"})
    @Query("""
            select u from User u
            where (:search IS NULL
                   OR lower(u.username)  like lower(concat('%', CAST(:search AS String), '%'))
                   OR lower(u.email)     like lower(concat('%', CAST(:search AS String), '%'))
                   OR lower(u.firstName) like lower(concat('%', CAST(:search AS String), '%'))
                   OR lower(u.lastName)  like lower(concat('%', CAST(:search AS String), '%')))
              AND (:isActive IS NULL OR u.isActive = :isActive)
            """)
    Page<User> findAllWithFilters(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
