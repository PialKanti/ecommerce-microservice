package com.example.ecommerce.product.repository;

import com.example.ecommerce.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCode(String code);

    @Query("""
            SELECT c FROM Category c
            WHERE (:search IS NULL
                   OR lower(c.name) LIKE lower(concat('%', :search, '%'))
                   OR lower(c.code) LIKE lower(concat('%', :search, '%')))
              AND (:isActive IS NULL OR c.isActive = :isActive)
            """)
    Page<Category> findAllWithFilters(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
