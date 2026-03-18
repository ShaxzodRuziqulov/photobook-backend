package com.example.photobook.service;

import com.example.photobook.dto.UserTaskDto;
import com.example.photobook.dto.UserTaskUpdateDto;
import com.example.photobook.dto.request.UserTaskPagingRequest;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
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
        validateActionableTask(order, assignment);

        if (dto.getProcessedCount() != null) {
            if (dto.getProcessedCount() < assignment.getProcessedCount()) {
                throw new IllegalArgumentException("processed_count cannot be decreased");
            }
            if (dto.getProcessedCount() < 0) {
                throw new IllegalArgumentException("processed_count must be greater than or equal to 0");
            }
            if (dto.getProcessedCount() > order.getAmount()) {
                throw new IllegalArgumentException("processed_count cannot be greater than amount");
            }
            OrderEmployee previousAssignment = findPreviousAssignment(order, assignment);
            if (previousAssignment != null && dto.getProcessedCount() > previousAssignment.getProcessedCount()) {
                throw new IllegalArgumentException("processed_count cannot exceed previous step progress");
            }
            assignment.setProcessedCount(dto.getProcessedCount());
        }

        if (dto.getNotes() != null) {
            String trimmed = dto.getNotes().trim();
            order.setNotes(trimmed.isEmpty() ? null : trimmed);
        }

        if (dto.getWorkStatus() != null) {
            validateWorkStatusTransition(assignment.getWorkStatus(), dto.getWorkStatus());
            if (dto.getWorkStatus() == EmployeeWorkStatus.COMPLETED &&
                    assignment.getProcessedCount() < order.getAmount()) {
                throw new IllegalArgumentException("Current step cannot be completed before full amount is processed");
            }
            assignment.setWorkStatus(dto.getWorkStatus());
            moveWorkflow(order, assignment);
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

    private void validateActionableTask(Order order, OrderEmployee assignment) {
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only in-progress orders can be updated");
        }
        if (assignment.getWorkStatus() != EmployeeWorkStatus.STARTED) {
            throw new IllegalArgumentException("Only the current active task can be updated");
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
        dto.setOrderProcessedCount(calculateOrderProcessedCount(order));
        dto.setStepOrder(assignment.getStepOrder());
        dto.setWorkStatus(assignment.getWorkStatus());
        dto.setCanWork(order.getStatus() == OrderStatus.IN_PROGRESS &&
                assignment.getWorkStatus() == EmployeeWorkStatus.STARTED);
        dto.setAcceptedDate(order.getAcceptedDate());
        dto.setDeadline(order.getDeadline());
        dto.setStatus(order.getStatus());
        dto.setImageUrl(order.getImageUrl());
        dto.setNotes(order.getNotes());
        return dto;
    }

    private void validateWorkStatusTransition(EmployeeWorkStatus from, EmployeeWorkStatus to) {
        if (from == to) {
            return;
        }
        boolean valid = switch (from) {
            case PENDING -> false;
            case STARTED -> to == EmployeeWorkStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid employee work status transition");
        }
    }

    private void moveWorkflow(Order order, OrderEmployee assignment) {
        if (assignment.getWorkStatus() != EmployeeWorkStatus.COMPLETED) {
            return;
        }

        OrderEmployee nextAssignment = order.getEmployees().stream()
                .filter(employee -> employee.getStepOrder() > assignment.getStepOrder())
                .sorted(java.util.Comparator.comparing(OrderEmployee::getStepOrder))
                .findFirst()
                .orElse(null);

        if (nextAssignment == null) {
            order.setStatus(OrderStatus.COMPLETED);
            return;
        }

        nextAssignment.setWorkStatus(EmployeeWorkStatus.STARTED);
        order.setStatus(OrderStatus.IN_PROGRESS);
    }

    private OrderEmployee findPreviousAssignment(Order order, OrderEmployee assignment) {
        return order.getEmployees().stream()
                .filter(employee -> employee.getStepOrder() < assignment.getStepOrder())
                .sorted(java.util.Comparator.comparing(OrderEmployee::getStepOrder).reversed())
                .findFirst()
                .orElse(null);
    }

    private int calculateOrderProcessedCount(Order order) {
        return order.getEmployees().stream()
                .sorted(java.util.Comparator.comparing(OrderEmployee::getStepOrder))
                .reduce((first, second) -> second)
                .map(OrderEmployee::getProcessedCount)
                .orElse(0);
    }
}
