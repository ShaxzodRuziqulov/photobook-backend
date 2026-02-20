package com.example.photobook.dto;

import com.example.photobook.entity.enumirated.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class OrderStatusHistoryDto  {
    private UUID id;
    private UUID orderId;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private UUID changedById;
    private LocalDateTime changedAt ;
}
