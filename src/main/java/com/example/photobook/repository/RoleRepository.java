package com.example.photobook.repository;

import com.example.photobook.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String name);

    @Query("""
            SELECT r
            FROM Role r
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY r.updatedAt DESC
            """)
    Page<Role> findPage(@Param("search") String search, Pageable pageable);
}
