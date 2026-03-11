package com.example.photobook.repository;

import com.example.photobook.entity.Order;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
            SELECT o
            FROM Order o
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(o.orderName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.receiverName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.customer.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.employee.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.category.name, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:kind IS NULL OR o.kind = :kind)
              AND (:status IS NULL OR o.status = :status)
              AND (:customerId IS NULL OR o.customer.id = :customerId)
              AND (:employeeId IS NULL OR o.employee.id = :employeeId)
              AND (:categoryId IS NULL OR o.category.id = :categoryId)
              AND (:from IS NULL OR o.acceptedDate >= :from)
              AND (:to IS NULL OR o.acceptedDate <= :to)
              AND (:deadlineFrom IS NULL OR o.deadline >= :deadlineFrom)
              AND (:deadlineTo IS NULL OR o.deadline <= :deadlineTo)
            ORDER BY o.updatedAt DESC
            """)
    Page<Order> findPage(@Param("search") String search,
                         @Param("kind") OrderKind kind,
                         @Param("status") OrderStatus status,
                         @Param("customerId") UUID customerId,
                         @Param("employeeId") UUID employeeId,
                         @Param("categoryId") UUID categoryId,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         @Param("deadlineFrom") LocalDate deadlineFrom,
                         @Param("deadlineTo") LocalDate deadlineTo,
                         Pageable pageable);
}
