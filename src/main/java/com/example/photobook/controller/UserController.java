package com.example.photobook.controller;

import com.example.photobook.dto.UserDto;
import com.example.photobook.dto.UserProfileUpdateDto;
import com.example.photobook.dto.UserRoleUpdateDto;
import com.example.photobook.dto.request.PageResponse;
import com.example.photobook.dto.request.UserPagingRequest;
import com.example.photobook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable UUID id, @RequestBody UserDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        return ResponseEntity.ok(service.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@RequestBody UserProfileUpdateDto dto) {
        return ResponseEntity.ok(service.updateCurrentUserProfile(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping("/paging")
    public ResponseEntity<PageResponse<UserDto>> paging(
            @RequestBody UserPagingRequest request,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(service.findPage(request, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UserDto> updateRoles(@PathVariable UUID id, @RequestBody UserRoleUpdateDto dto) {
        return ResponseEntity.ok(service.updateRoles(id, dto));
    }
}
