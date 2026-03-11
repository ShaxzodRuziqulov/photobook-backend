package com.example.photobook.dto.request;

import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class OrderPagingRequest extends BasePagingRequest {
    private String search;
    private OrderKind kind;
    private OrderStatus status;
    private UUID customerId;
    private UUID employeeId;
    private UUID categoryId;
    private LocalDate from;
    private LocalDate to;
    private LocalDate deadlineFrom;
    private LocalDate deadlineTo;
}
