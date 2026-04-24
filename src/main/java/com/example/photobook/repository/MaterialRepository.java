package com.example.photobook.repository;

import com.example.photobook.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {
    @Query("""
            SELECT m
            FROM Material m
            WHERE (:itemType IS NULL OR :itemType = '' OR LOWER(COALESCE(m.itemType, '')) = LOWER(:itemType))
            ORDER BY m.updatedAt DESC
            """)
    Page<Material> findPageWithoutTextSearch(@Param("itemType") String itemType, Pageable pageable);

    @Query("""
            SELECT m
            FROM Material m
            WHERE (LOWER(m.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(m.itemType, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:itemType IS NULL OR :itemType = '' OR LOWER(COALESCE(m.itemType, '')) = LOWER(:itemType))
            ORDER BY m.updatedAt DESC
            """)
    Page<Material> findPageWithTextSearch(@Param("search") String search,
                                          @Param("itemType") String itemType,
                                          Pageable pageable);
}
