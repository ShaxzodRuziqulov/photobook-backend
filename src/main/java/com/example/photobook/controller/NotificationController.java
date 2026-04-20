package com.example.photobook.controller;

import com.example.photobook.dto.NotificationDto;
import com.example.photobook.dto.NotificationUnreadCountDto;
import com.example.photobook.dto.request.NotificationPagingRequest;
import com.example.photobook.dto.request.PageResponse;
import com.example.photobook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<List<NotificationDto>> findMyNotifications() {
        return ResponseEntity.ok(notificationService.findMyNotifications());
    }

    @PostMapping("/me/paging")
    public ResponseEntity<PageResponse<NotificationDto>> findMyNotificationsPage(
            @RequestBody(required = false) NotificationPagingRequest request,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(notificationService.findMyNotificationsPage(request, pageable)));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<NotificationUnreadCountDto> getMyUnreadCount() {
        return ResponseEntity.ok(notificationService.getMyUnreadCount());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
