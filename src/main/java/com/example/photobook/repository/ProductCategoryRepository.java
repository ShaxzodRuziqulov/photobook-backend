package com.example.photobook.repository;

import com.example.photobook.entity.ProductCategory;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.projection.OrderCategoryCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for product category persistence and category dashboard aggregations.
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    List<ProductCategory> findByKindOrderByNameAsc(OrderKind kind);

    @Query("""
            SELECT c.id AS categoryId,
                   c.name AS categoryName,
                   COALESCE(SUM(finalAssignment.processedCount), 0) AS count
            FROM ProductCategory c
            LEFT JOIN Order o ON o.category = c
            LEFT JOIN OrderEmployee finalAssignment
                   ON finalAssignment.order = o
                  AND finalAssignment.stepOrder = (
                      SELECT MAX(assignment.stepOrder)
                      FROM OrderEmployee assignment
                      WHERE assignment.order = o
                  )
            WHERE c.kind = :kind
            GROUP BY c.id, c.name
            ORDER BY c.name ASC
            """)
    List<OrderCategoryCountProjection> countOrdersByCategory(@Param("kind") OrderKind kind);

    @Query("""
            SELECT p
            FROM ProductCategory p
            WHERE (:kind IS NULL OR p.kind = :kind)
              AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.updatedAt DESC
            """)
    Page<ProductCategory> findPage(@Param("kind") OrderKind kind, @Param("search") String search, Pageable pageable);
}
