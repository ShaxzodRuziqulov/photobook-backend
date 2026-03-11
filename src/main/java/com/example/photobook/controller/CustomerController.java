package com.example.photobook.controller;

import com.example.photobook.dto.CustomerDto;
import com.example.photobook.dto.request.CustomerPagingRequest;
import com.example.photobook.dto.request.PageResponse;
import com.example.photobook.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService service;

    @PostMapping
    public ResponseEntity<CustomerDto> create(@RequestBody CustomerDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CustomerDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping("/paging")
    public ResponseEntity<PageResponse<CustomerDto>> paging(
            @RequestBody CustomerPagingRequest request,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(service.findPage(request, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
