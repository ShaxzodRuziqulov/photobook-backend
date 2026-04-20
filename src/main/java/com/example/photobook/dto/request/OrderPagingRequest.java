package com.example.photobook.dto.request;

import com.example.photobook.entity.enumirated.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrderPagingRequest {
    private String search;
    private OrderStatus status;
    private LocalDate acceptedDate;
    private LocalDate deadline;
}
