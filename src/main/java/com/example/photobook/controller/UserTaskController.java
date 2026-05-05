package com.example.photobook.controller;

import com.example.photobook.dto.UserTaskDto;
import com.example.photobook.dto.UserTaskUpdateDto;
import com.example.photobook.dto.request.PageResponse;
import com.example.photobook.dto.request.UserTaskPagingRequest;
import com.example.photobook.projection.MyCategoryMonthlyStatsProjection;
import com.example.photobook.service.UserTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-tasks")
public class UserTaskController {
    private final UserTaskService service;

    @GetMapping("/me/stats/by-category")
    public ResponseEntity<List<MyCategoryMonthlyStatsProjection>> myStatsByCategory() {
        return ResponseEntity.ok(service.getMyCategoryMonthlyStats());
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<UserTaskDto> myTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getUserTaskById(id));
    }

    @PostMapping("/me/paging")
    public ResponseEntity<PageResponse<UserTaskDto>> myTasksPaging(
            @RequestBody UserTaskPagingRequest request,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(new PageResponse<>(service.findMyTasksPage(request, pageable)));
    }

    @PutMapping("/me/{id}")
    public ResponseEntity<UserTaskDto> updateMyTask(@PathVariable UUID id, @RequestBody UserTaskUpdateDto dto) {
        return ResponseEntity.ok(service.updateMyTask(id, dto));
    }
}
