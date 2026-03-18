package com.example.photobook.repository;

import com.example.photobook.entity.ProductCategory;
import com.example.photobook.entity.enumirated.OrderKind;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("""
            SELECT p
            FROM ProductCategory p
            WHERE (:kind IS NULL OR p.kind = :kind)
              AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.updatedAt DESC
            """)
    Page<ProductCategory> findPage(@Param("kind") OrderKind kind, @Param("search") String search, Pageable pageable);
}
