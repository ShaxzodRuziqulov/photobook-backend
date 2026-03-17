package com.example.photobook.service;

import com.example.photobook.dto.UserTaskDto;
import com.example.photobook.dto.UserTaskUpdateDto;
import com.example.photobook.dto.request.UserTaskPagingRequest;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserTaskService {
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    public UserTaskDto getUserTaskById(UUID id) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Order order = findOwnedTask(id, currentUserId);
        return toDto(order, findAssignment(order, currentUserId));
    }

    public Page<UserTaskDto> findMyTasksPage(UserTaskPagingRequest request, Pageable pageable) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        String search = normalizeSearch(request.getSearch());
        List<OrderStatus> statuses = normalizeStatuses(request.getStatuses());

        return orderRepository.findTasksPageByEmployeeId(
                currentUserId,
                statuses,
                request.getFrom(),
                request.getTo(),
                request.getDeadlineFrom(),
                request.getDeadlineTo(),
                search,
                pageable
        ).map(order -> toDto(order, findAssignment(order, currentUserId)));
    }

    public UserTaskDto updateMyTask(UUID id, UserTaskUpdateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Order order = findOwnedTask(id, currentUserId);
        OrderEmployee assignment = findAssignment(order, currentUserId);

        if (dto.getProcessedCount() != null) {
            if (dto.getProcessedCount() < 0) {
                throw new IllegalArgumentException("processed_count must be greater than or equal to 0");
            }
            if (dto.getProcessedCount() > order.getAmount()) {
                throw new IllegalArgumentException("processed_count cannot be greater than amount");
            }
            assignment.setProcessedCount(dto.getProcessedCount());
        }

        if (dto.getNotes() != null) {
            String trimmed = dto.getNotes().trim();
            order.setNotes(trimmed.isEmpty() ? null : trimmed);
        }

        if (dto.getStatus() != null) {
            validateStatusTransition(order.getStatus(), dto.getStatus());
            order.setStatus(dto.getStatus());
        }

        Order saved = orderRepository.save(order);
        return toDto(saved, findAssignment(saved, currentUserId));
    }

    private List<OrderStatus> normalizeStatuses(List<OrderStatus> statuses) {
        return statuses == null || statuses.isEmpty() ? null : statuses;
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Order findOwnedTask(UUID orderId, UUID currentUserId) {
        return orderRepository.findTaskByIdAndEmployeeId(orderId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("task not found"));
    }

    private OrderEmployee findAssignment(Order order, UUID currentUserId) {
        return order.getEmployees().stream()
                .filter(employee -> employee.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("task assignment not found"));
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return;
        }
        boolean valid = switch (from) {
            case PENDING -> to == OrderStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == OrderStatus.PENDING || to == OrderStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid order status transition");
        }
    }

    private UserTaskDto toDto(Order order, OrderEmployee assignment) {
        UserTaskDto dto = new UserTaskDto();
        dto.setOrderId(order.getId());
        dto.setKind(order.getKind());
        dto.setCategoryId(order.getCategory() == null ? null : order.getCategory().getId());
        dto.setCategoryName(order.getCategory() == null ? null : order.getCategory().getName());
        dto.setOrderName(order.getOrderName());
        dto.setItemType(order.getItemType());
        dto.setCustomerId(order.getCustomer() == null ? null : order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer() == null ? null : order.getCustomer().getFullName());
        dto.setReceiverName(order.getReceiverName());
        dto.setPageCount(order.getPageCount());
        dto.setAmount(order.getAmount());
        dto.setProcessedCount(assignment.getProcessedCount());
        dto.setAcceptedDate(order.getAcceptedDate());
        dto.setDeadline(order.getDeadline());
        dto.setStatus(order.getStatus());
        dto.setImageUrl(order.getImageUrl());
        dto.setNotes(order.getNotes());
        return dto;
    }
}
