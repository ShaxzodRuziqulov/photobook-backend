package com.example.photobook.controller;

import com.example.photobook.dto.EmployeeOrderSummaryDto;
import com.example.photobook.dto.WorkLogResponseDto;
import com.example.photobook.projection.MonthlyWorkSummaryProjection;
import com.example.photobook.service.OrderWorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-logs")
@RequiredArgsConstructor
public class WorkLogController {

    private final OrderWorkLogService service;

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyWorkSummaryProjection>> getMonthlyReport(
            @RequestParam String month) {
        return ResponseEntity.ok(service.getMonthlyReport(month));
    }

    @GetMapping("/my-monthly")
    public ResponseEntity<Long> getMyMonthlyTotal(@RequestParam String month) {
        return ResponseEntity.ok(service.getMyMonthlyTotal(month));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<WorkLogResponseDto>> getMyHistory() {
        return ResponseEntity.ok(service.getMyHistory());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<WorkLogResponseDto>> getOrderLogs(@PathVariable UUID orderId) {
        return ResponseEntity.ok(service.getOrderLogs(orderId));
    }

    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<List<WorkLogResponseDto>> getEmployeeHistory(
            @PathVariable UUID employeeId,
            @RequestParam String month) {
        return ResponseEntity.ok(service.getEmployeeHistory(employeeId, month));
    }

    @GetMapping("/employee/{employeeId}/monthly-orders")
    public ResponseEntity<List<EmployeeOrderSummaryDto>> getEmployeeMonthlyOrders(
            @PathVariable UUID employeeId,
            @RequestParam String month) {
        return ResponseEntity.ok(service.getEmployeeMonthlyOrders(employeeId, month));
    }
}
