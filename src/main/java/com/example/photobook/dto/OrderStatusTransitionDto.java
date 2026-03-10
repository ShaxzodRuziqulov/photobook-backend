package com.example.photobook.dto;

import com.example.photobook.entity.enumirated.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusTransitionDto {
    private OrderStatus toStatus;
}
