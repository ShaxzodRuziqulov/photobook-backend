package com.example.photobook.repository;

import com.example.photobook.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("""
            SELECT e
            FROM Expense e
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
              AND (:materialId IS NULL OR e.material.id = :materialId)
              AND (:paymentMethod IS NULL OR :paymentMethod = '' OR LOWER(COALESCE(e.paymentMethod, '')) = LOWER(:paymentMethod))
            ORDER BY e.updatedAt DESC
            """)
    Page<Expense> findPage(@Param("search") String search, @Param("categoryId") UUID categoryId,
                           @Param("materialId") UUID materialId, @Param("paymentMethod") String paymentMethod,
                           Pageable pageable);
}
