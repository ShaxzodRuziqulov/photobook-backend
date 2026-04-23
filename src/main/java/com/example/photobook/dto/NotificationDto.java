package com.example.photobook.dto;

import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import com.example.photobook.entity.enumirated.NotificationTargetType;
import com.example.photobook.entity.enumirated.NotificationType;
import com.example.photobook.entity.enumirated.OrderKind;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationDto {
    private UUID id;
    private NotificationType type;
    private String title;
    private String message;
    private UUID orderId;
    private String orderName;
    private OrderKind orderKind;
    private UUID employeeId;
    private String employeeName;
    private Integer stepOrder;
    private EmployeeWorkStatus workStatus;
    private NotificationTargetType targetType;
    private UUID targetId;
    private OrderKind targetKind;
    private String route;
    private Boolean actionRequired;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
