package com.example.photobook.service;

import com.example.photobook.dto.NotificationDto;
import com.example.photobook.dto.NotificationUnreadCountDto;
import com.example.photobook.dto.request.NotificationPagingRequest;
import com.example.photobook.entity.Notification;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.User;
import com.example.photobook.repository.NotificationRepository;
import com.example.photobook.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public Notification create(
            User user,
            String type,
            String title,
            String message,
            Order order,
            OrderEmployee assignment,
            boolean actionRequired
    ) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setOrderId(order == null ? null : order.getId());
        notification.setOrderName(order == null ? null : order.getOrderName());
        notification.setOrderKind(order == null ? null : order.getKind().name());
        notification.setEmployeeId(assignment == null ? null : assignment.getUser().getId());
        notification.setEmployeeName(assignment == null ? null : buildFullName(assignment.getUser()));
        notification.setStepOrder(assignment == null ? null : assignment.getStepOrder());
        notification.setWorkStatus(assignment == null || assignment.getWorkStatus() == null
                ? null
                : assignment.getWorkStatus().name());
        notification.setTargetType(order == null ? null : "ORDER");
        notification.setTargetId(order == null ? null : order.getId());
        notification.setTargetKind(order == null ? null : order.getKind().name());
        notification.setRoute(order == null ? null : routeForOrder(order));
        notification.setActionRequired(actionRequired);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findMyNotifications() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> findMyNotificationsPage(NotificationPagingRequest request, Pageable pageable) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        NotificationPagingRequest safeRequest = request == null ? new NotificationPagingRequest() : request;
        return notificationRepository.findPage(
                currentUserId,
                safeRequest.getSearch(),
                safeRequest.getType(),
                safeRequest.getIsRead(),
                safeRequest.getActionRequired(),
                pageable
        ).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public NotificationUnreadCountDto getMyUnreadCount() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return new NotificationUnreadCountDto(notificationRepository.countByUserIdAndReadAtIsNull(currentUserId));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findUnreadByUserId(UUID userId) {
        return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtAsc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public NotificationDto markAsRead(UUID id) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("notification not found"));
        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("notification not found");
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return toDto(notificationRepository.save(notification));
    }

    public void markAllAsRead() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        List<Notification> notifications = notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtAsc(currentUserId);
        if (notifications.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        notifications.forEach(notification -> notification.setReadAt(now));
        notificationRepository.saveAll(notifications);
    }

    public void deleteByOrderId(UUID orderId) {
        if (orderId == null) {
            return;
        }
        notificationRepository.deleteByOrderId(orderId);
    }

    public void deleteByOrderIdAndUserIds(UUID orderId, Collection<UUID> userIds) {
        if (orderId == null || userIds == null || userIds.isEmpty()) {
            return;
        }
        notificationRepository.deleteByOrderIdAndUserIdIn(orderId, userIds);
    }

    public NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setOrderId(notification.getOrderId());
        dto.setOrderName(notification.getOrderName());
        dto.setOrderKind(notification.getOrderKind());
        dto.setEmployeeId(notification.getEmployeeId());
        dto.setEmployeeName(notification.getEmployeeName());
        dto.setStepOrder(notification.getStepOrder());
        dto.setWorkStatus(notification.getWorkStatus());
        dto.setTargetType(notification.getTargetType());
        dto.setTargetId(notification.getTargetId());
        dto.setTargetKind(notification.getTargetKind());
        dto.setRoute(notification.getRoute());
        dto.setActionRequired(notification.getActionRequired());
        dto.setReadAt(notification.getReadAt());
        dto.setIsRead(notification.getReadAt() != null);
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    private String routeForOrder(Order order) {
        return switch (order.getKind()) {
            case ALBUM -> "/album";
            case VIGNETTE -> "/vignette";
            case PICTURE -> "/picture";
        };
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
}
