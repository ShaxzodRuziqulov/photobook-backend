package com.example.photobook.controller;

import com.example.photobook.dto.OrderDto;
import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.dto.OrderStatusTransitionDto;
import com.example.photobook.service.OrderService;
import com.example.photobook.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService service;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody OrderDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> update(@PathVariable UUID id, @RequestBody OrderDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> changeStatus(@PathVariable UUID id, @RequestBody OrderStatusTransitionDto dto) {
        return ResponseEntity.ok(service.changeStatus(id, dto, currentUserService.getCurrentUserId()));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<OrderStatusHistoryDto>> getStatusHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getStatusHistory(id));
    }
}
