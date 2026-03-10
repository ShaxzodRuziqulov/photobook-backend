package com.example.photobook.controller;

import com.example.photobook.dto.DashboardAmountTrendDto;
import com.example.photobook.dto.DashboardCountDto;
import com.example.photobook.dto.DashboardSummaryDto;
import com.example.photobook.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> summary(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(dashboardService.getSummary(from, to));
    }

    @GetMapping("/orders-by-status")
    public ResponseEntity<List<DashboardCountDto>> ordersByStatus() {
        return ResponseEntity.ok(dashboardService.getOrdersByStatus());
    }

    @GetMapping("/orders-by-kind")
    public ResponseEntity<List<DashboardCountDto>> ordersByKind() {
        return ResponseEntity.ok(dashboardService.getOrdersByKind());
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<List<DashboardAmountTrendDto>> revenueTrend() {
        return ResponseEntity.ok(dashboardService.getRevenueTrend());
    }

    @GetMapping("/expenses-trend")
    public ResponseEntity<List<DashboardAmountTrendDto>> expensesTrend() {
        return ResponseEntity.ok(dashboardService.getExpensesTrend());
    }
}
