package com.example.photobook.controller;

import com.example.photobook.dto.ExpenseCategoryDto;
import com.example.photobook.service.ExpenseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense-category")
public class ExpenseCategoryController {
    private final ExpenseCategoryService service;

    @PostMapping("/create")
    public ResponseEntity<ExpenseCategoryDto> create(@RequestBody ExpenseCategoryDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ExpenseCategoryDto> update(@PathVariable UUID id, @RequestBody ExpenseCategoryDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseCategoryDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExpenseCategoryDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
