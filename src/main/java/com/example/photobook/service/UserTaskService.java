package com.example.photobook.service;

import com.example.photobook.dto.CategoryStatsDto;
import com.example.photobook.dto.UserTaskDto;
import com.example.photobook.dto.UserTaskUpdateDto;
import com.example.photobook.dto.request.UserTaskPagingRequest;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.service.security.CurrentUserService;
import com.example.photobook.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserTaskService {
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;
    private final SocketIoService socketIoService;
    private final OrderWorkLogService workLogService;

    public UserTaskDto getUserTaskById(UUID id) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Order order = findOwnedTask(id, currentUserId);
        return toDto(order, findAssignment(order, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<UserTaskDto> findMyTasksPage(UserTaskPagingRequest request, Pageable pageable) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        String search = normalizeSearch(request.getSearch());
        List<OrderStatus> statuses = request.getStatuses() != null && !request.getStatuses().isEmpty()
                ? request.getStatuses()
                : List.of(OrderStatus.values());
        Pageable effectivePageable = withDefaultSort(pageable);

        LocalDate deadlineFrom = request.getDeadlineFrom() != null ? request.getDeadlineFrom() : LocalDate.of(1900, 1, 1);
        LocalDate deadlineTo = request.getDeadlineTo() != null ? request.getDeadlineTo() : LocalDate.of(9999, 12, 31);
        LocalDate acceptedDateFrom = request.getAcceptedDateFrom() != null ? request.getAcceptedDateFrom() : LocalDate.of(1900, 1, 1);
        LocalDate acceptedDateTo = request.getAcceptedDateTo() != null ? request.getAcceptedDateTo() : LocalDate.of(9999, 12, 31);

        return orderRepository.findMyTasks(
                currentUserId,
                statuses,
                deadlineFrom,
                deadlineTo,
                acceptedDateFrom,
                acceptedDateTo,
                search != null ? search : "",
                effectivePageable
        ).map(order -> toDto(order, findAssignment(order, currentUserId)));
    }

    @Transactional(readOnly = true)
    public List<CategoryStatsDto> getMyCategoryMonthlyStats(String month) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return orderRepository.findMyCategoryMonthlyStats(currentUserId, month)
                .stream()
                .map(p -> new CategoryStatsDto(
                        p.getCategoryId(),
                        p.getCategoryName(),
                        p.getKind(),
                        p.getDefaultPages(),
                        p.getWorkMonth(),
                        p.getOrderCount(),
                        p.getTotalProcessed()))
                .collect(Collectors.toList());
    }

    public UserTaskDto updateMyTask(UUID id, UserTaskUpdateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Order order = findOwnedTask(id, currentUserId);
        OrderEmployee assignment = findAssignment(order, currentUserId);
        OrderStatus orderStatusBefore = order.getStatus();
        Map<Integer, EmployeeWorkStatus> workStatusByStepBefore = snapshotWorkStatusByStep(order);
        UUID previousActiveEmployeeId = getActiveEmployeeId(order);
        int targetProgress = getTargetProgress(order);
        refreshPipelineWorkflow(order, targetProgress);
        validateActionableTask(order, assignment);

        if (dto.getProcessedCount() != null) {
            if (dto.getProcessedCount() < 0) {
                throw new IllegalArgumentException("processed_count must be greater than or equal to 0");
            }

            int stepTotalBefore = calculateStepTotalProcessed(order, assignment.getStepOrder());
            int nextStepTotal = stepTotalBefore + dto.getProcessedCount();

            if (nextStepTotal > targetProgress) {
                throw new IllegalArgumentException("Step total progress cannot exceed order amount");
            }

            int previousStepTotal = calculatePreviousStepTotal(order, assignment.getStepOrder());
            if (assignment.getStepOrder() > 1 && nextStepTotal > previousStepTotal) {
                throw new IllegalArgumentException("Step progress cannot exceed previous step total progress");
            }

            int previousCount = assignment.getProcessedCount();
            int nextProcessedCount = previousCount + dto.getProcessedCount();
            assignment.setProcessedCount(nextProcessedCount);

            workLogService.logProgress(order.getId(), currentUserId, assignment.getStepOrder(), previousCount, nextProcessedCount, dto.getNotes());
        }

        if (dto.getNotes() != null) {
            String trimmed = dto.getNotes().trim();
            assignment.setNotes(trimmed.isEmpty() ? null : trimmed);
        }

        if (dto.getWorkStatus() != null) {
            EmployeeWorkStatus requestedStatus = dto.getWorkStatus();
            if (requestedStatus == EmployeeWorkStatus.COMPLETED) {
                int stepTotal = calculateStepTotalProcessed(order, assignment.getStepOrder());
                if (stepTotal < targetProgress) {
                    requestedStatus = assignment.getWorkStatus();
                }
            }
            validateWorkStatusTransition(assignment.getWorkStatus(), requestedStatus);
            assignment.setWorkStatus(requestedStatus);
        }

        refreshPipelineWorkflow(order, targetProgress);
        Order saved = orderRepository.save(order);
        notifyAdminsForNewlyCompletedSteps(saved, workStatusByStepBefore, orderStatusBefore);
        if (saved.getStatus() == OrderStatus.COMPLETED && orderStatusBefore != OrderStatus.COMPLETED) {
            OrderEmployee lastStep = findLastStepAssignment(saved);
            if (lastStep != null) {
                socketIoService.notifyAdminsOrderWorkCompleted(saved, lastStep);
            }
        }
        notifyNextEmployeeIfChanged(saved, previousActiveEmployeeId);
        return toDto(saved, findAssignment(saved, currentUserId));
    }

    private OrderEmployee findLastStepAssignment(Order order) {
        if (order.getEmployees() == null || order.getEmployees().isEmpty()) {
            return null;
        }
        return order.getEmployees().stream()
                .max(Comparator.comparing(OrderEmployee::getStepOrder))
                .orElse(null);
    }

    private Map<Integer, EmployeeWorkStatus> snapshotWorkStatusByStep(Order order) {
        if (order.getEmployees() == null) {
            return Map.of();
        }
        return order.getEmployees().stream()
                .filter(employee -> employee.getStepOrder() != null)
                .collect(Collectors.toMap(OrderEmployee::getStepOrder, OrderEmployee::getWorkStatus, (left, right) -> left));
    }

    private void notifyAdminsForNewlyCompletedSteps(
            Order order,
            Map<Integer, EmployeeWorkStatus> workStatusByStepBefore,
            OrderStatus orderStatusBefore
    ) {
        if (order.getEmployees() == null) {
            return;
        }
        Integer maxStepOrder = order.getEmployees().stream()
                .map(OrderEmployee::getStepOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        boolean orderJustFullyCompleted =
                order.getStatus() == OrderStatus.COMPLETED && orderStatusBefore != OrderStatus.COMPLETED;

        for (OrderEmployee employee : order.getEmployees()) {
            Integer step = employee.getStepOrder();
            if (step == null) {
                continue;
            }
            EmployeeWorkStatus before = workStatusByStepBefore.get(step);
            if (before != EmployeeWorkStatus.COMPLETED && employee.getWorkStatus() == EmployeeWorkStatus.COMPLETED) {
                if (orderJustFullyCompleted && maxStepOrder != null && maxStepOrder.equals(step)) {
                    continue;
                }
                socketIoService.notifyAdminsTaskStepCompleted(order, employee);
            }
        }
    }

    private String normalizeSearch(String search) {
        return StringUtils.normalize(search);
    }

    private Pageable withDefaultSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "updatedAt"));
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
        dto.setStepTotalProcessed(calculateStepTotalProcessed(order, assignment.getStepOrder()));
        dto.setOrderProcessedCount(calculateOrderProcessedCount(order));
        dto.setAvailableToProcess(calculatePreviousStepTotal(order, assignment.getStepOrder()));
        dto.setRemainingAvailable(dto.getAvailableToProcess() - dto.getStepTotalProcessed());
        dto.setRemainingTotal(order.getAmount() - dto.getStepTotalProcessed());
        dto.setStepOrder(assignment.getStepOrder());
        dto.setWorkStatus(assignment.getWorkStatus());
        dto.setCanWork(order.getStatus() == OrderStatus.IN_PROGRESS &&
                assignment.getWorkStatus() == EmployeeWorkStatus.STARTED);
        dto.setAcceptedDate(order.getAcceptedDate());
        dto.setDeadline(order.getDeadline());
        dto.setStatus(order.getStatus());
        dto.setImageUrl(order.getImageUrl());
        dto.setNotes(assignment.getNotes());
        dto.setOrderNotes(order.getNotes());
        return dto;
    }

    private void validateWorkStatusTransition(EmployeeWorkStatus from, EmployeeWorkStatus to) {
        if (from == to) {
            return;
        }
        boolean valid = switch (from) {
            case PENDING, COMPLETED -> false;
            case STARTED -> to == EmployeeWorkStatus.COMPLETED;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid employee work status transition");
        }
    }

    private OrderEmployee findPreviousAssignment(Order order, OrderEmployee assignment) {
        return order.getEmployees().stream()
                .filter(employee -> employee.getStepOrder() < assignment.getStepOrder()).max(java.util.Comparator.comparing(OrderEmployee::getStepOrder))
                .orElse(null);
    }

    private int calculateOrderProcessedCount(Order order) {
        Integer maxStep = order.getEmployees().stream()
                .map(OrderEmployee::getStepOrder)
                .max(Integer::compareTo)
                .orElse(null);
        if (maxStep == null) return 0;
        return calculateStepTotalProcessed(order, maxStep);
    }

    private int calculateStepTotalProcessed(Order order, int stepOrder) {
        return order.getEmployees().stream()
                .filter(e -> e.getStepOrder() != null && e.getStepOrder() == stepOrder)
                .mapToInt(e -> e.getProcessedCount() == null ? 0 : e.getProcessedCount())
                .sum();
    }

    private int calculatePreviousStepTotal(Order order, int stepOrder) {
        if (stepOrder <= 1) return getTargetProgress(order);
        return order.getEmployees().stream()
                .filter(e -> e.getStepOrder() < stepOrder)
                .collect(Collectors.groupingBy(OrderEmployee::getStepOrder, Collectors.summingInt(e -> e.getProcessedCount() == null ? 0 : e.getProcessedCount())))
                .values().stream()
                .min(Integer::compareTo) // In a strict pipeline, it should be the min of previous steps or just the immediate previous
                .orElse(0);
    }

    private int calculateAvailableToProcess(Order order, OrderEmployee assignment) {
        return calculatePreviousStepTotal(order, assignment.getStepOrder());
    }

    private int getTargetProgress(Order order) {
        return order.getAmount() == null ? 0 : order.getAmount();
    }

    private int safeProcessedCount(OrderEmployee assignment) {
        return assignment.getProcessedCount() == null ? 0 : assignment.getProcessedCount();
    }

    private UUID getActiveEmployeeId(Order order) {
        return order.getEmployees().stream()
                .filter(employee -> employee.getWorkStatus() == EmployeeWorkStatus.STARTED)
                .map(employee -> employee.getUser().getId())
                .findFirst()
                .orElse(null);
    }

    private void notifyNextEmployeeIfChanged(Order order, UUID previousActiveEmployeeId) {
        OrderEmployee activeAssignment = socketIoService.findActiveAssignment(order);
        if (activeAssignment == null) {
            return;
        }

        UUID currentActiveEmployeeId = activeAssignment.getUser().getId();
        if (currentActiveEmployeeId.equals(previousActiveEmployeeId)) {
            return;
        }

        socketIoService.notifyTaskActivated(order, activeAssignment);
    }

    private void refreshPipelineWorkflow(Order order, int targetProgress) {
        List<OrderEmployee> employees = order.getEmployees();
        if (employees == null || employees.isEmpty()) return;

        Map<Integer, Integer> stepTotals = employees.stream()
                .filter(e -> e.getStepOrder() != null)
                .collect(Collectors.groupingBy(OrderEmployee::getStepOrder,
                        Collectors.summingInt(e -> e.getProcessedCount() == null ? 0 : e.getProcessedCount())));

        List<Integer> sortedSteps = stepTotals.keySet().stream().sorted().toList();
        boolean allCompleted = true;

        for (int i = 0; i < sortedSteps.size(); i++) {
            int step = sortedSteps.get(i);
            int total = stepTotals.get(step);
            
            final EmployeeWorkStatus status;
            if (total >= targetProgress) {
                status = EmployeeWorkStatus.COMPLETED;
            } else {
                allCompleted = false;
                if (i == 0) {
                    status = EmployeeWorkStatus.STARTED;
                } else {
                    int prevTotal = stepTotals.get(sortedSteps.get(i - 1));
                    status = (prevTotal > total) ? EmployeeWorkStatus.STARTED : EmployeeWorkStatus.PENDING;
                }
            }
            
            // Apply status to all employees in this step
            employees.stream()
                    .filter(e -> e.getStepOrder() == step)
                    .forEach(e -> e.setWorkStatus(status));
        }

        order.setStatus(allCompleted ? OrderStatus.COMPLETED : OrderStatus.IN_PROGRESS);
    }
}
