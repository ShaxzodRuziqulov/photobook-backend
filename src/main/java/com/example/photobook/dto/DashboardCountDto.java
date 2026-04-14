package com.example.photobook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardCountDto {
    private long count;
    private String kind;
    private String status;
    private String category;
}
