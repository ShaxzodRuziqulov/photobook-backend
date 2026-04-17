package com.example.photobook.controller;

import com.example.photobook.dto.DashboardCategoryCountDto;
import com.example.photobook.dto.DashboardKeyCountDto;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.service.DashboardService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for optimized dashboard statistics.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/orders-by-kind")
    public ResponseEntity<List<DashboardKeyCountDto>> ordersByKind() {
        return ResponseEntity.ok(dashboardService.getOrdersByKind());
    }

    @GetMapping("/orders-by-status")
    public ResponseEntity<List<DashboardKeyCountDto>> ordersByStatus(@RequestParam @NotNull OrderKind type) {
        return ResponseEntity.ok(dashboardService.getOrdersByStatus(type));
    }

    @GetMapping("/orders-by-category")
    public ResponseEntity<List<DashboardCategoryCountDto>> ordersByCategory(@RequestParam @NotNull OrderKind type) {
        return ResponseEntity.ok(dashboardService.getOrdersByCategory(type));
    }
}
