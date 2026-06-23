package com.example.ecommerce.product.repository;

import com.example.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @Query("""
            SELECT p FROM Product p
            WHERE (:search IS NULL
                   OR lower(p.name) LIKE lower(concat('%', CAST(:search AS String), '%'))
                   OR lower(p.sku)  LIKE lower(concat('%', CAST(:search AS String), '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:isActive IS NULL OR p.isActive = :isActive)
            """)
    Page<Product> findAllWithFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
