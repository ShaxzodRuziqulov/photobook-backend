package com.example.photobook.repository;

import com.example.photobook.entity.Order;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.projection.MyCategoryMonthlyStatsProjection;
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
            WHERE o.deleted = false
            GROUP BY o.kind
            """)
    List<OrderKindCountProjection> countOrdersByKind();

    @Query("""
            SELECT o.status AS status, COUNT(o.id) AS count
            FROM Order o
            WHERE o.kind = :kind AND o.deleted = false
            GROUP BY o.status
            """)
    List<OrderStatusCountProjection> countOrdersByStatus(@Param("kind") OrderKind kind);

    @Query("""
            SELECT o
            FROM Order o
            WHERE o.deleted = false
              AND o.status = COALESCE(:status, o.status)
              AND o.acceptedDate <= :deadlineTo
              AND o.deadline >= :acceptedDateFrom
            ORDER BY o.updatedAt DESC
            """)
    Page<Order> findPageWithoutTextSearch(@Param("status") OrderStatus status,
                                          @Param("acceptedDateFrom") LocalDate acceptedDateFrom,
                                          @Param("deadlineTo") LocalDate deadlineTo,
                                          Pageable pageable);

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN o.employees assignment
            LEFT JOIN assignment.user employee
            WHERE o.deleted = false
              AND (LOWER(o.orderName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.receiverName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.customer.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(CONCAT(
                       COALESCE(employee.lastName, ''), ' ',
                       COALESCE(employee.firstName, '')
                   )) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(employee.username, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.category.name, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND o.status = COALESCE(:status, o.status)
              AND o.acceptedDate <= :deadlineTo
              AND o.deadline >= :acceptedDateFrom
            ORDER BY o.updatedAt DESC
            """)
    Page<Order> findPageWithTextSearch(@Param("search") String search,
                                       @Param("status") OrderStatus status,
                                       @Param("acceptedDateFrom") LocalDate acceptedDateFrom,
                                       @Param("deadlineTo") LocalDate deadlineTo,
                                       Pageable pageable);

    @Query(value = """
            SELECT DISTINCT o
            FROM Order o
            JOIN o.employees assignment
            WHERE assignment.user.id = :userId
              AND o.deleted = false
              AND o.status IN :statuses
              AND o.deadline >= :deadlineFrom
              AND o.deadline <= :deadlineTo
              AND o.acceptedDate >= :acceptedDateFrom
              AND o.acceptedDate <= :acceptedDateTo
              AND (LOWER(o.orderName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.receiverName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.customer.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.category.name, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY o.updatedAt DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT o)
            FROM Order o
            JOIN o.employees assignment
            WHERE assignment.user.id = :userId
              AND o.deleted = false
              AND o.status IN :statuses
              AND o.deadline >= :deadlineFrom
              AND o.deadline <= :deadlineTo
              AND o.acceptedDate >= :acceptedDateFrom
              AND o.acceptedDate <= :acceptedDateTo
              AND (LOWER(o.orderName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.receiverName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.customer.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(o.category.name, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Order> findMyTasks(@Param("userId") UUID userId,
                            @Param("statuses") List<OrderStatus> statuses,
                            @Param("deadlineFrom") LocalDate deadlineFrom,
                            @Param("deadlineTo") LocalDate deadlineTo,
                            @Param("acceptedDateFrom") LocalDate acceptedDateFrom,
                            @Param("acceptedDateTo") LocalDate acceptedDateTo,
                            @Param("search") String search,
                            Pageable pageable);

    @EntityGraph(attributePaths = {"category", "customer", "employees", "employees.user"})
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN o.employees assignment
            LEFT JOIN assignment.user employee
            WHERE o.deleted = false
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

    @Query(value = """
            SELECT o.category_id                          AS categoryId,
                   pc.name                               AS categoryName,
                   o.kind                                AS kind,
                   MAX(o.page_count)                     AS defaultPages,
                   TO_CHAR(o.accepted_date, 'YYYY-MM')  AS workMonth,
                   COUNT(DISTINCT wl.order_id)           AS orderCount,
                   COALESCE(SUM(wl.delta), 0)            AS totalProcessed
            FROM order_work_log wl
            JOIN orders o ON o.id = wl.order_id
            JOIN product_categories pc ON pc.id = o.category_id
            WHERE wl.employee_id = :userId
              AND o.deleted      = false
              AND (:month IS NULL OR TO_CHAR(o.accepted_date, 'YYYY-MM') = :month)
            GROUP BY o.category_id, pc.name, o.kind, TO_CHAR(o.accepted_date, 'YYYY-MM')
            ORDER BY pc.name, TO_CHAR(o.accepted_date, 'YYYY-MM') DESC
            """, nativeQuery = true)
    List<MyCategoryMonthlyStatsProjection> findMyCategoryMonthlyStats(@Param("userId") UUID userId,
                                                                       @Param("month") String month);
}
