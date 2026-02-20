package com.example.photobook.dto;


import com.example.photobook.entity.OrderStatusHistory;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderDto {

    private UUID id;

    private OrderKind kind;
    private UUID categoryId;
    private String orderName;

    private String itemType;

    private UUID customerId;
    private String receiverName;

    private UUID employeeId;

    private Integer pageCount;
    private Integer amount;

    private Integer processedCount;
    private LocalDate acceptedDate;

    private LocalDate deadline;
    private OrderStatus status;
    private List<OrderStatusHistory> statusHistory;
}
