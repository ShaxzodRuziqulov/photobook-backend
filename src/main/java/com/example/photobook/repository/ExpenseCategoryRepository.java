package com.example.photobook.repository;

import com.example.photobook.entity.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {
    @Query("""
            SELECT e
            FROM ExpenseCategory e
            WHERE (:search IS NULL OR :search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY e.updatedAt DESC
            """)
    Page<ExpenseCategory> findPage(@Param("search") String search, Pageable pageable);
}
