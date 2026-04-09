package com.example.photobook.controller;

import com.example.photobook.dto.RoleDto;
import com.example.photobook.dto.request.PageResponse;
import com.example.photobook.dto.request.RolePageRequest;
import com.example.photobook.service.RoleService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService service;

    @Hidden
    @PostMapping
    public ResponseEntity<RoleDto> create(@RequestBody RoleDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Hidden
    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> update(@PathVariable UUID id, @RequestBody RoleDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping("/paging")
    public ResponseEntity<PageResponse<RoleDto>> paging(
            @RequestBody RolePageRequest request,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(service.findPage(request, pageable)));
    }

    @Hidden
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
