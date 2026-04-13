package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationDto {
    private UUID id;
    private String type;
    private String title;
    private String message;
    private UUID orderId;
    private String orderName;
    private UUID employeeId;
    private String employeeName;
    private Integer stepOrder;
    private String workStatus;
    private Boolean actionRequired;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
