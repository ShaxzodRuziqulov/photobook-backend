package com.example.photobook.dto.request;

import com.example.photobook.entity.enumirated.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UserTaskPagingRequest {
    private String search;
    private List<OrderStatus> statuses;
    private LocalDate from;
    private LocalDate to;
    private LocalDate deadlineFrom;
    private LocalDate deadlineTo;
}
