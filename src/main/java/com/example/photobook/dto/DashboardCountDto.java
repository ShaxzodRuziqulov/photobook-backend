package com.example.photobook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardCountDto {
    private String key;
    private long count;
}
