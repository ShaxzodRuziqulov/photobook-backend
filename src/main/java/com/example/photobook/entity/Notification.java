package com.example.photobook.entity;

import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import com.example.photobook.entity.enumirated.NotificationTargetType;
import com.example.photobook.entity.enumirated.NotificationType;
import com.example.photobook.entity.enumirated.OrderKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_name", length = 200)
    private String orderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_kind", length = 20)
    private OrderKind orderKind;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @Column(name = "step_order")
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", length = 30)
    private EmployeeWorkStatus workStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30)
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_kind", length = 30)
    private OrderKind targetKind;

    @Column(length = 255)
    private String route;

    @Column(name = "action_required", nullable = false)
    private Boolean actionRequired = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
