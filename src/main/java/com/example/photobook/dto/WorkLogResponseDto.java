package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class WorkLogResponseDto {
    private UUID id;
    private UUID orderId;
    private String orderName;
    private UUID employeeId;
    private String employeeFullName;
    private Integer stepOrder;
    private Integer delta;
    private Integer snapshot;
    private String workMonth;
    private LocalDateTime loggedAt;
    private String notes;
}
