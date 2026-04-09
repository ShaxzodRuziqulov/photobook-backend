package com.example.photobook.controller;

import com.example.photobook.dto.request.PageResponse;
import com.example.photobook.dto.ProductCategoryDto;
import com.example.photobook.dto.request.ProductCategoryPagingRequest;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-categories")
public class ProductCategoryController {
    private final ProductCategoryService service;

    @PostMapping
    public ResponseEntity<ProductCategoryDto> create(@RequestBody ProductCategoryDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDto> update(@PathVariable UUID id, @RequestBody ProductCategoryDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductCategoryDto>> findAll(@RequestParam(required = false) OrderKind kind) {
        return ResponseEntity.ok(service.findAll(kind));
    }

    @PostMapping("/paging")
    public ResponseEntity<PageResponse<ProductCategoryDto>> paging(
            @RequestBody ProductCategoryPagingRequest request,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(service.findPage(request, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
