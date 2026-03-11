package com.example.photobook.repository;

import com.example.photobook.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query("""
            SELECT e
            FROM Employee e
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(e.profession, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(e.phoneNumber, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:profession IS NULL OR :profession = '' OR LOWER(COALESCE(e.profession, '')) = LOWER(:profession))
              AND (:isActive IS NULL OR e.isActive = :isActive)
            ORDER BY e.updatedAt DESC
            """)
    Page<Employee> findPage(@Param("search") String search,
                            @Param("profession") String profession,
                            @Param("isActive") Boolean isActive,
                            Pageable pageable
    );
}
