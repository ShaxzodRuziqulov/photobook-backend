package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardSummaryDto {
    private long ordersTotal;
    private long ordersDone;
    private long ordersInProgress;
    private BigDecimal revenueTotal;
    private BigDecimal expensesTotal;
    private BigDecimal profit;
}
