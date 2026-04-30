package com.example.photobook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "order_work_log",
        indexes = {
                @Index(name = "idx_work_log_employee_month", columnList = "employee_id, work_month"),
                @Index(name = "idx_work_log_order", columnList = "order_id")
        }
)
public class OrderWorkLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private User employee;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(nullable = false)
    private Integer delta;

    @Column(nullable = false)
    private Integer snapshot;

    @Column(name = "work_month", nullable = false, length = 7)
    private String workMonth;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;
}
