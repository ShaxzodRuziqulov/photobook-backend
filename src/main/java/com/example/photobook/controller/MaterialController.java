package com.example.photobook.controller;

import com.example.photobook.dto.MaterialDto;
import com.example.photobook.dto.MaterialAdjustDto;
import com.example.photobook.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/materials")
public class MaterialController {
    private final MaterialService service;

    @PostMapping
    public ResponseEntity<MaterialDto> create(@RequestBody MaterialDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialDto> update(@PathVariable UUID id, @RequestBody MaterialDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<MaterialDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/adjust")
    public ResponseEntity<MaterialDto> adjust(@PathVariable UUID id, @RequestBody MaterialAdjustDto dto) {
        return ResponseEntity.ok(service.adjust(id, dto));
    }
}
