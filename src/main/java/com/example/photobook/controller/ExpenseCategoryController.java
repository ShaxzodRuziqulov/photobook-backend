package com.example.photobook.controller;

import com.example.photobook.dto.ExpenseCategoryDto;
import com.example.photobook.dto.request.PageResponse;
import com.example.photobook.dto.request.ExpenseCategoryPagingRequest;
import com.example.photobook.service.ExpenseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expense-categories")
public class ExpenseCategoryController {
    private final ExpenseCategoryService service;

    @PostMapping
    public ResponseEntity<ExpenseCategoryDto> create(@RequestBody ExpenseCategoryDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseCategoryDto> update(@PathVariable UUID id, @RequestBody ExpenseCategoryDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseCategoryDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseCategoryDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping("/paging")
    public ResponseEntity<PageResponse<ExpenseCategoryDto>> paging(
            @RequestBody ExpenseCategoryPagingRequest request,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(service.findPage(request, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
