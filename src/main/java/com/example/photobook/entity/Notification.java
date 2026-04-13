package com.example.photobook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_user_id", columnList = "user_id"),
                @Index(name = "idx_notifications_read_at", columnList = "read_at")
        }
)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_name", length = 200)
    private String orderName;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @Column(name = "step_order")
    private Integer stepOrder;

    @Column(name = "work_status", length = 30)
    private String workStatus;

    @Column(name = "action_required", nullable = false)
    private Boolean actionRequired = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
