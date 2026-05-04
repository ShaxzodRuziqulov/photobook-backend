package com.example.photobook.repository;

import com.example.photobook.entity.OrderWorkLog;
import com.example.photobook.projection.EmployeeMonthlyOrderProjection;
import com.example.photobook.projection.MonthlyWorkSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderWorkLogRepository extends JpaRepository<OrderWorkLog, UUID> {

    @Query("""
            SELECT SUM(l.delta)
            FROM OrderWorkLog l
            JOIN l.order o
            WHERE l.employeeId = :employeeId 
              AND l.workMonth = :month 
              AND o.deleted = false
            """)
    Long sumDeltaByEmployeeAndMonth(@Param("employeeId") UUID employeeId, @Param("month") String month);

    @Query("""
            SELECT l.employeeId AS employeeId, 
                   TRIM(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) AS employeeFullName,
                   SUM(l.delta) AS totalDelta
            FROM OrderWorkLog l
            JOIN l.employee u
            JOIN l.order o
            WHERE l.workMonth = :month AND o.deleted = false
            GROUP BY l.employeeId, u.firstName, u.lastName
            """)
    List<MonthlyWorkSummaryProjection> monthlyReport(@Param("month") String month);

    Optional<OrderWorkLog> findTopByOrderIdAndEmployeeIdAndStepOrderOrderByLoggedAtDesc(
            UUID orderId, UUID employeeId, Integer stepOrder);

    List<OrderWorkLog> findByOrderIdOrderByLoggedAtDesc(UUID orderId);

    List<OrderWorkLog> findByEmployeeIdOrderByLoggedAtDesc(UUID employeeId);

    List<OrderWorkLog> findByEmployeeIdAndWorkMonthOrderByLoggedAtDesc(UUID employeeId, String month);

    @Query("""
            SELECT o.id             AS orderId,
                   o.orderName      AS orderName,
                   o.category.name  AS category,
                   o.itemType       AS itemType,
                   o.status         AS status,
                   o.acceptedDate   AS acceptedDate,
                   o.kind           AS kind,
                   MAX(l.loggedAt)  AS lastLoggedAt,
                   SUM(l.delta)     AS totalDelta
            FROM OrderWorkLog l
            JOIN l.order o
            WHERE l.employeeId = :employeeId
              AND l.workMonth  = :month
              AND o.deleted    = false
            GROUP BY o.id, o.orderName, o.category.name, o.itemType, o.status, o.acceptedDate
            ORDER BY MAX(l.loggedAt) DESC
            """)
    List<EmployeeMonthlyOrderProjection> findOrderSummaryByEmployeeAndMonth(
            @Param("employeeId") UUID employeeId,
            @Param("month") String month);
}
