package com.example.photobook.controller;

import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.service.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order-status-histories")
public class OrderStatusHistoryController {
    private final OrderStatusHistoryService service;

    @PostMapping
    public ResponseEntity<OrderStatusHistoryDto> create(@RequestBody OrderStatusHistoryDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderStatusHistoryDto> update(@PathVariable UUID id, @RequestBody OrderStatusHistoryDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderStatusHistoryDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderStatusHistoryDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
