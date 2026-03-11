package com.example.photobook.repository;

import com.example.photobook.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @Query("""
            SELECT c
            FROM Customer c
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR 
                   LOWER(c.notes) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:isActive IS NULL OR c.isActive = :isActive)
            ORDER BY c.updatedAt DESC
            """)
    Page<Customer> findPage(@Param("search") String search,
                            @Param("isActive") Boolean isActive,
                            Pageable pageable);
}
