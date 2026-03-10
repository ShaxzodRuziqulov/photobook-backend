package com.example.photobook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DashboardAmountTrendDto {
    private String period;
    private BigDecimal amount;
}
