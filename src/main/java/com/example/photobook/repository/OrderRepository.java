package com.example.photobook.repository;

import com.example.photobook.entity.Order;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.projection.OrderKindCountProjection;
import com.example.photobook.projection.OrderStatusCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
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
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

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
            SELECT o
            FROM Order o
            WHERE o.status = COALESCE(:status, o.status)
              AND o.acceptedDate = COALESCE(:acceptedDate, o.acceptedDate)
              AND o.deadline = COALESCE(:deadline, o.deadline)
            ORDER BY o.updatedAt DESC
            """)
    Page<Order> findPageWithoutTextSearch(@Param("status") OrderStatus status,
                                          @Param("acceptedDate") LocalDate acceptedDate,
                                          @Param("deadline") LocalDate deadline,
                                          Pageable pageable);

    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN o.employees assignment
            LEFT JOIN assignment.user employee
            WHERE (LOWER(o.orderName) LIKE LOWER(CONCAT('%', :search, '%')) OR
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
    Page<Order> findPageWithTextSearch(@Param("search") String search,
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

    @Override
    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    Page<Order> findAll(Specification<Order> specification, Pageable pageable);
}
