package com.example.photobook.service;

import com.example.photobook.dto.UserTaskDto;
import com.example.photobook.dto.UserTaskUpdateDto;
import com.example.photobook.dto.request.UserTaskPagingRequest;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
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
        return toDto(order);
    }

    public Page<UserTaskDto> findMyTasksPage(UserTaskPagingRequest request, Pageable pageable) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        String search = normalizeSearch(request.getSearch());
        List<OrderStatus> statuses = normalizeStatuses(request.getStatuses());

        if (search == null) {
            return orderRepository.findTasksPageByEmployeeId(
                    currentUserId,
                    statuses,
                    request.getFrom(),
                    request.getTo(),
                    request.getDeadlineFrom(),
                    request.getDeadlineTo(),
                    pageable
            ).map(this::toDto);
        }

        List<UserTaskDto> filtered = orderRepository.findTasksByEmployeeId(
                        currentUserId,
                        statuses,
                        request.getFrom(),
                        request.getTo(),
                        request.getDeadlineFrom(),
                        request.getDeadlineTo()
                ).stream()
                .filter(order -> matchesSearch(order, search))
                .map(this::toDto)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<UserTaskDto> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);
        return new PageImpl<>(content, pageable, filtered.size());
    }

    public UserTaskDto updateMyTask(UUID id, UserTaskUpdateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Order order = findOwnedTask(id, currentUserId);

        if (dto.getProcessedCount() != null) {
            if (dto.getProcessedCount() < 0) {
                throw new IllegalArgumentException("processed_count must be greater than or equal to 0");
            }
            if (dto.getProcessedCount() > order.getAmount()) {
                throw new IllegalArgumentException("processed_count cannot be greater than amount");
            }
            order.setProcessedCount(dto.getProcessedCount());
        }

        if (dto.getNotes() != null) {
            String trimmed = dto.getNotes().trim();
            order.setNotes(trimmed.isEmpty() ? null : trimmed);
        }

        if (dto.getStatus() != null) {
            validateStatusTransition(order.getStatus(), dto.getStatus());
            order.setStatus(dto.getStatus());
        }

        return toDto(orderRepository.save(order));
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

    private boolean matchesSearch(Order order, String search) {
        if (search == null) {
            return true;
        }
        String needle = search.toLowerCase(Locale.ROOT);
        return contains(order.getOrderName(), needle)
                || contains(order.getReceiverName(), needle)
                || contains(order.getCustomer() == null ? null : order.getCustomer().getFullName(), needle)
                || contains(order.getCategory() == null ? null : order.getCategory().getName(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private Order findOwnedTask(UUID orderId, UUID currentUserId) {
        return orderRepository.findTaskByIdAndEmployeeId(orderId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("task not found"));
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

    private UserTaskDto toDto(Order order) {
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
        dto.setProcessedCount(order.getProcessedCount());
        dto.setAcceptedDate(order.getAcceptedDate());
        dto.setDeadline(order.getDeadline());
        dto.setStatus(order.getStatus());
        dto.setImageUrl(order.getImageUrl());
        dto.setNotes(order.getNotes());
        return dto;
    }
}
