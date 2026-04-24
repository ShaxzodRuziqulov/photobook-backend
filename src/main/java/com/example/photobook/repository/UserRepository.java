package com.example.photobook.repository;

import com.example.photobook.entity.Customer;
import com.example.photobook.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    @Query("""
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN u.roles r
            WHERE (:isActive IS NULL OR u.isActive = :isActive)
              AND (:role IS NULL OR :role = '' OR LOWER(r.name) = LOWER(:role))
            ORDER BY u.updatedAt DESC
            """)
    Page<User> findPageWithoutTextSearch(@Param("isActive") Boolean isActive,
                                         @Param("role") String role,
                                         Pageable pageable);

    @EntityGraph(attributePaths = "roles")
    @Query("""
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN u.roles r
            WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(u.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(u.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(u.profession, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:isActive IS NULL OR u.isActive = :isActive)
              AND (:role IS NULL OR :role = '' OR LOWER(r.name) = LOWER(:role))
            ORDER BY u.updatedAt DESC
            """)
    Page<User> findPageWithTextSearch(@Param("search") String search,
                                      @Param("isActive") Boolean isActive,
                                      @Param("role") String role,
                                      Pageable pageable);

    @Query("""
            SELECT u
            FROM User u WHERE u.isActive = true
            """)
    List<User> findAllIsActive();

    @EntityGraph(attributePaths = "roles")
    @Query("""
            SELECT DISTINCT u
            FROM User u
            JOIN u.roles r
            WHERE u.isActive = true
              AND r.name IN ('ROLE_ADMIN', 'ROLE_MANAGER')
            """)
    List<User> findAllActiveAdminsAndManagers();
}
