package com.example.photobook.repository;

import com.example.photobook.entity.Order;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.projection.OrderKindCountProjection;
import com.example.photobook.projection.OrderStatusCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for order persistence and optimized order dashboard aggregations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
            SELECT o.kind AS kind, COUNT(o.id) AS count
            FROM Order o
            GROUP BY o.kind
            """)
    List<OrderKindCountProjection> countOrdersByKind();

    @Query("""
            SELECT o.status AS status, COUNT(o.id) AS count
            FROM Order o
            WHERE o.kind = :kind
            GROUP BY o.status
            """)
    List<OrderStatusCountProjection> countOrdersByStatus(@Param("kind") OrderKind kind);

    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN o.employees assignment
            LEFT JOIN assignment.user employee
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(o.orderName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.receiverName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.customer.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(CONCAT(
                       COALESCE(employee.lastName, ''), ' ',
                       COALESCE(employee.firstName, '')
                   )) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(employee.username, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.category.name, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND o.status = COALESCE(:status, o.status)
              AND o.acceptedDate = COALESCE(:acceptedDate, o.acceptedDate)
              AND o.deadline = COALESCE(:deadline, o.deadline)
            ORDER BY o.updatedAt DESC
            """)
    Page<Order> findPage(@Param("search") String search,
                         @Param("status") OrderStatus status,
                         @Param("acceptedDate") LocalDate acceptedDate,
                         @Param("deadline") LocalDate deadline,
                         Pageable pageable);

    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    Optional<Order> findById(UUID id);

    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN o.employees assignment
            LEFT JOIN assignment.user employee
            ORDER BY o.updatedAt DESC
            """)
    List<Order> findAllWithDetails();

    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    @Query("""
            SELECT o
            FROM Order o
            JOIN o.employees assignment
            JOIN assignment.user employee
            WHERE o.id = :orderId
              AND employee.id = :employeeId
            """)
    Optional<Order> findTaskByIdAndEmployeeId(@Param("orderId") UUID orderId,
                                              @Param("employeeId") UUID employeeId);

    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN o.employees assignment
            JOIN assignment.user employee
            WHERE employee.id = :employeeId
              AND (:statuses IS NULL OR o.status IN :statuses)
              AND (:from IS NULL OR o.acceptedDate >= :from)
              AND (:to IS NULL OR o.acceptedDate <= :to)
              AND (:deadlineFrom IS NULL OR o.deadline >= :deadlineFrom)
              AND (:deadlineTo IS NULL OR o.deadline <= :deadlineTo)
              AND (
                         :search IS NULL OR
                         LOWER(CAST(o.orderName AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                         LOWER(CAST(COALESCE(o.receiverName,'') AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                       )
            ORDER BY o.updatedAt DESC
            """)
    Page<Order> findTasksPageByEmployeeId(
            @Param("employeeId") UUID employeeId,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("deadlineFrom") LocalDate deadlineFrom,
            @Param("deadlineTo") LocalDate deadlineTo,
            @Param("search") String search,
            Pageable pageable
    );
}
