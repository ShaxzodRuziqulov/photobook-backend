package com.example.photobook.service;

import com.example.photobook.dto.UserTaskDto;
import com.example.photobook.dto.UserTaskUpdateDto;
import com.example.photobook.dto.request.UserTaskPagingRequest;
import com.example.photobook.entity.Customer;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.ProductCategory;
import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.service.security.CurrentUserService;
import com.example.photobook.util.StringUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public UserTaskDto getUserTaskById(UUID id) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Order order = findOwnedTask(id, currentUserId);
        return toDto(order, findAssignment(order, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<UserTaskDto> findMyTasksPage(UserTaskPagingRequest request, Pageable pageable) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        String search = normalizeSearch(request.getSearch());
        List<OrderStatus> statuses = normalizeStatuses(request.getStatuses());
        Pageable effectivePageable = withDefaultSort(pageable);

        return orderRepository.findAll(
                buildMyTasksSpecification(currentUserId, request, statuses, search),
                effectivePageable
        ).map(order -> toDto(order, findAssignment(order, currentUserId)));
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
            int nextProcessedCount = assignment.getProcessedCount() + dto.getProcessedCount();
            if (nextProcessedCount > targetProgress) {
                throw new IllegalArgumentException("processed_count increment cannot exceed remaining amount");
            }
            OrderEmployee previousAssignment = findPreviousAssignment(order, assignment);
            if (previousAssignment != null && nextProcessedCount > previousAssignment.getProcessedCount()) {
                throw new IllegalArgumentException("processed_count increment cannot exceed previous step progress");
            }
            assignment.setProcessedCount(nextProcessedCount);
        }

        if (dto.getNotes() != null) {
            String trimmed = dto.getNotes().trim();
            assignment.setNotes(trimmed.isEmpty() ? null : trimmed);
        }

        if (dto.getWorkStatus() != null) {
            validateWorkStatusTransition(assignment.getWorkStatus(), dto.getWorkStatus());
            if (dto.getWorkStatus() == EmployeeWorkStatus.COMPLETED &&
                    assignment.getProcessedCount() < targetProgress) {
                throw new IllegalArgumentException("Current step cannot be completed before full amount is processed");
            }
            assignment.setWorkStatus(dto.getWorkStatus());
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

    private List<OrderStatus> normalizeStatuses(List<OrderStatus> statuses) {
        return statuses == null || statuses.isEmpty() ? null : statuses;
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

    private Specification<Order> buildMyTasksSpecification(
            UUID currentUserId,
            UserTaskPagingRequest request,
            List<OrderStatus> statuses,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Subquery<Integer> assignedToMe = query.subquery(Integer.class);
            Root<OrderEmployee> assignmentRoot = assignedToMe.from(OrderEmployee.class);
            assignedToMe.select(criteriaBuilder.literal(1));
            assignedToMe.where(
                    criteriaBuilder.equal(assignmentRoot.get("order"), root),
                    criteriaBuilder.equal(assignmentRoot.get("user").get("id"), currentUserId)
            );
            predicates.add(criteriaBuilder.exists(assignedToMe));

            if (statuses != null) {
                predicates.add(root.get("status").in(statuses));
            }
            if (request.getDeadlineFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("deadline"), request.getDeadlineFrom()));
            }
            if (request.getDeadlineTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("deadline"), request.getDeadlineTo()));
            }
            if (search != null) {
                Join<Order, Customer> customer = root.join("customer", JoinType.INNER);
                Join<Order, ProductCategory> category = root.join("category", JoinType.INNER);
                String likeSearch = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("orderName")), likeSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("receiverName")), likeSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(customer.get("fullName")), likeSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(category.get("name")), likeSearch)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
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
        dto.setAvailableToProcess(calculateAvailableToProcess(order, assignment));
        dto.setRemainingAvailable(calculateRemainingAvailable(order, assignment));
        dto.setRemainingTotal(calculateRemainingTotal(order, assignment));
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
        return order.getEmployees().stream()
                .sorted(java.util.Comparator.comparing(OrderEmployee::getStepOrder))
                .reduce((first, second) -> second)
                .map(OrderEmployee::getProcessedCount)
                .orElse(0);
    }

    private int getTargetProgress(Order order) {
        return order.getAmount() == null ? 0 : order.getAmount();
    }

    private int calculateAvailableToProcess(Order order, OrderEmployee assignment) {
        OrderEmployee previousAssignment = findPreviousAssignment(order, assignment);
        if (previousAssignment == null) {
            return getTargetProgress(order);
        }
        return previousAssignment.getProcessedCount() == null ? 0 : previousAssignment.getProcessedCount();
    }

    private int calculateRemainingAvailable(Order order, OrderEmployee assignment) {
        int remaining = calculateAvailableToProcess(order, assignment) - safeProcessedCount(assignment);
        return Math.max(remaining, 0);
    }

    private int calculateRemainingTotal(Order order, OrderEmployee assignment) {
        int remaining = getTargetProgress(order) - safeProcessedCount(assignment);
        return Math.max(remaining, 0);
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
        List<OrderEmployee> assignments = order.getEmployees().stream()
                .sorted(Comparator.comparing(OrderEmployee::getStepOrder))
                .toList();

        if (assignments.isEmpty()) {
            return;
        }

        boolean allCompleted = true;

        for (int i = 0; i < assignments.size(); i++) {
            OrderEmployee current = assignments.get(i);

            if (current.getProcessedCount() >= targetProgress) {
                current.setWorkStatus(EmployeeWorkStatus.COMPLETED);
                continue;
            }

            allCompleted = false;

            if (i == 0) {
                current.setWorkStatus(EmployeeWorkStatus.STARTED);
                continue;
            }

            OrderEmployee previous = assignments.get(i - 1);
            if (previous.getProcessedCount() > current.getProcessedCount()) {
                current.setWorkStatus(EmployeeWorkStatus.STARTED);
            } else {
                current.setWorkStatus(EmployeeWorkStatus.PENDING);
            }
        }

        order.setStatus(allCompleted ? OrderStatus.COMPLETED : OrderStatus.IN_PROGRESS);
    }
}
