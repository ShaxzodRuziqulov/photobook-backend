package com.example.photobook.service;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.dto.OrderDto;
import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.dto.OrderStatusTransitionDto;
import com.example.photobook.dto.request.OrderPagingRequest;
import com.example.photobook.entity.*;
import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.entity.enumirated.OwnerType;
import com.example.photobook.mapper.OrderMapper;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductCategoryService productCategoryService;
    private final CustomerService customerService;
    private final UserService userService;
    private final OrderStatusHistoryService historyService;
    private final UploadService uploadService;
    private final NotificationService notificationService;
    private final SocketIoService socketIoService;

    public OrderDto create(OrderDto dto) {
        List<EmployeeDto> employees = dto.getEmployees();
        validateOrder(dto, employees);

        Order order = mapper.toEntity(dto);
        fillOrderFields(order, dto, employees);

        Order saved = repository.save(order);
        attachUpload(saved, dto.getUploadId());
        socketIoService.notifyOrderAssigned(saved);

        return toDto(saved);
    }

    public OrderDto update(UUID id, OrderDto dto) {
        List<EmployeeDto> employees = dto.getEmployees();
        validateOrder(dto, employees);

        Order order = findByOrderId(id);
        Set<UUID> removedUserIds = fillOrderFields(order, dto, employees);

        Order saved = repository.save(order);
        attachUpload(saved, dto.getUploadId());
        notificationService.deleteByOrderIdAndUserIds(saved.getId(), removedUserIds);
        socketIoService.notifyOrderUpdated(saved);

        return toDto(saved);
    }

    public OrderDto findById(UUID id) {
        return toDto(findByOrderId(id));
    }

    public List<OrderDto> findAll() {
        return repository.findAllWithDetails().stream()
                .map(this::toDto)
                .toList();
    }

    public Page<OrderDto> findPage(OrderPagingRequest request, Pageable pageable) {
        return repository.findPage(
                request.getSearch(),
                request.getKind(),
                request.getStatus(),
                request.getCustomerId(),
                request.getEmployeeId(),
                request.getCategoryId(),
                request.getFrom(),
                request.getTo(),
                request.getDeadlineFrom(),
                request.getDeadlineTo(),
                pageable
        ).map(this::toDto);
    }

    public void delete(UUID id) {
        Order order = findByOrderId(id);

        uploadService.deleteOwnedUpload(
                OwnerType.ORDER,
                order.getId(),
                null
        );

        notificationService.deleteByOrderId(order.getId());
        repository.delete(order);
    }

    public OrderDto changeStatus(UUID id,
                                 OrderStatusTransitionDto dto,
                                 UUID changedById) {

        if (dto.getToStatus() == null) {
            throw new IllegalArgumentException("toStatus is required");
        }

        Order order = findByOrderId(id);

        OrderStatus from = order.getStatus();
        OrderStatus to = dto.getToStatus();

        if (from == to) {
            return toDto(order);
        }

        if (!isValidTransition(from, to)) {
            throw new IllegalArgumentException("Invalid order status transition");
        }

        if (to == OrderStatus.COMPLETED && hasIncompleteEmployees(order)) {
            throw new IllegalArgumentException("All employees must be completed before order is completed");
        }

        order.setStatus(to);
        alignWorkflow(order);

        Order saved = repository.save(order);

        OrderStatusHistoryDto historyDto = new OrderStatusHistoryDto();
        historyDto.setOrderId(saved.getId());
        historyDto.setFromStatus(from);
        historyDto.setToStatus(to);
        historyDto.setChangedById(changedById);
        historyDto.setChangedAt(LocalDateTime.now());
        historyService.create(historyDto);
        socketIoService.notifyOrderStatusChanged(saved, from, to);
        socketIoService.notifyTaskActivated(saved, socketIoService.findActiveAssignment(saved));

        return toDto(saved);
    }

    public List<OrderStatusHistoryDto> getStatusHistory(UUID orderId) {
        return historyService.findAllByOrderId(orderId);
    }

    public Order findByOrderId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
    }

    private Set<UUID> fillOrderFields(Order order, OrderDto dto, List<EmployeeDto> employees) {
        fillBasicFields(order, dto);
        resolveRelations(order, dto);
        Set<UUID> removedUserIds = syncEmployees(order, employees);
        applyWorkflow(order);
        return removedUserIds;
    }

    private void fillBasicFields(Order order, OrderDto dto) {
        order.setKind(dto.getKind());
        order.setOrderName(dto.getOrderName());
        order.setItemType(StringUtils.normalize(dto.getItemType()));
        order.setReceiverName(dto.getReceiverName().trim());
        order.setPageCount(dto.getPageCount());
        order.setAmount(dto.getAmount());
        order.setAcceptedDate(dto.getAcceptedDate());
        order.setDeadline(dto.getDeadline());
        order.setStatus(dto.getStatus());
        order.setNotes(StringUtils.normalize(dto.getNotes()));
        if (dto.getImageUrl() != null) {
            order.setImageUrl(StringUtils.normalize(dto.getImageUrl()));
        }
    }

    private void resolveRelations(Order order, OrderDto dto) {
        order.setCustomer(customerService.resolveForOrder(dto.getCustomerId(), dto.getCustomerName()));
        order.setCategory(productCategoryService.findByProductCategoryId(dto.getCategoryId()));
    }

    private void applyWorkflow(Order order) {
        alignWorkflow(order);
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        Map<OrderStatus, Set<OrderStatus>> transitions = Map.of(
                OrderStatus.PENDING, Set.of(OrderStatus.IN_PROGRESS, OrderStatus.PAUSED),
                OrderStatus.IN_PROGRESS, Set.of(OrderStatus.PAUSED, OrderStatus.COMPLETED),
                OrderStatus.PAUSED, Set.of(OrderStatus.IN_PROGRESS, OrderStatus.COMPLETED),
                OrderStatus.COMPLETED, Set.of()
        );

        return transitions
                .getOrDefault(from, Set.of())
                .contains(to);
    }

    private void validateOrder(OrderDto dto, List<EmployeeDto> employees) {
        validateOrderFields(dto);
        validateEmployees(employees);
        validateEmployeeWorkflow(employees);
    }

    private void validateOrderFields(OrderDto dto) {
        if (dto.getKind() == null) {
            throw new IllegalArgumentException("kind is required");
        }
        if (dto.getOrderName() == null || dto.getOrderName().isBlank()) {
            throw new IllegalArgumentException("order_name is required");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("category_id is required");
        }
        if (dto.getCustomerId() == null &&
                (dto.getCustomerName() == null || dto.getCustomerName().isBlank())) {
            throw new IllegalArgumentException("customer_id or customer_name is required");
        }
        if (dto.getReceiverName() == null || dto.getReceiverName().isBlank()) {
            throw new IllegalArgumentException("receiver_name is required");
        }
        if (dto.getPageCount() == null || dto.getPageCount() < 0) {
            throw new IllegalArgumentException("page_count must be >= 0");
        }
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (dto.getAcceptedDate() == null) {
            throw new IllegalArgumentException("accepted_date is required");
        }
        if (dto.getDeadline() == null) {
            throw new IllegalArgumentException("deadline is required");
        }
        if (dto.getDeadline().isBefore(dto.getAcceptedDate())) {
            throw new IllegalArgumentException("deadline cannot be earlier than accepted_date");
        }
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
    }

    private void validateEmployees(List<EmployeeDto> employees) {
        if (employees == null || employees.isEmpty()) {
            throw new IllegalArgumentException("order must have at least one employee");
        }
        if (employees.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("employees contains invalid value");
        }
        if (employees.stream().anyMatch(e -> e.getEmployeeId() == null)) {
            throw new IllegalArgumentException("employees.employeeId is required");
        }
        if (employees.stream().anyMatch(e -> e.getProcessedCount() != null && e.getProcessedCount() < 0)) {
            throw new IllegalArgumentException("employees.processedCount must be >= 0");
        }
        long distinctEmployeeCount = employees.stream()
                .map(EmployeeDto::getEmployeeId)
                .distinct()
                .count();
        if (distinctEmployeeCount != employees.size()) {
            throw new IllegalArgumentException("employees must be unique per order");
        }
    }

    private void validateEmployeeWorkflow(List<EmployeeDto> employees) {
        if (employees.stream().anyMatch(e -> e.getStepOrder() == null)) {
            throw new IllegalArgumentException("employees.stepOrder is required");
        }
        long distinctStepCount = employees.stream()
                .map(EmployeeDto::getStepOrder)
                .distinct()
                .count();
        if (distinctStepCount != employees.size()) {
            throw new IllegalArgumentException("employees.stepOrder must be unique");
        }
        List<Integer> steps = employees.stream()
                .map(EmployeeDto::getStepOrder)
                .sorted()
                .toList();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i) != i + 1) {
                throw new IllegalArgumentException("stepOrder must be sequential starting from 1");
            }
        }
    }

    private List<OrderEmployee> resolveEmployees(List<EmployeeDto> employees) {
        List<UUID> employeeIds = employees.stream()
                .map(EmployeeDto::getEmployeeId)
                .distinct()
                .toList();

        Map<UUID, User> usersById = userService.findAllByIds(employeeIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return employees.stream()
                .map(employeeDto -> toOrderEmployee(employeeDto, usersById))
                .toList();
    }

    private Set<UUID> syncEmployees(Order order, List<EmployeeDto> employees) {
        List<OrderEmployee> resolvedEmployees = resolveEmployees(employees);
        Map<UUID, OrderEmployee> existingByUserId = order.getEmployees().stream()
                .collect(Collectors.toMap(
                        assignment -> assignment.getUser().getId(),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Set<UUID> incomingUserIds = resolvedEmployees.stream()
                .map(assignment -> assignment.getUser().getId())
                .collect(Collectors.toSet());

        Set<UUID> removedUserIds = order.getEmployees().stream()
                .map(existing -> existing.getUser().getId())
                .filter(existingUserId -> !incomingUserIds.contains(existingUserId))
                .collect(Collectors.toSet());

        order.getEmployees().removeIf(existing -> !incomingUserIds.contains(existing.getUser().getId()));

        for (OrderEmployee resolvedEmployee : resolvedEmployees) {
            UUID userId = resolvedEmployee.getUser().getId();
            OrderEmployee existing = existingByUserId.get(userId);
            if (existing != null) {
                existing.setStepOrder(resolvedEmployee.getStepOrder());
                continue;
            }
            order.addEmployee(resolvedEmployee);
        }

        return removedUserIds;
    }

    private OrderEmployee toOrderEmployee(EmployeeDto employeeDto, Map<UUID, User> usersById) {
        User user = usersById.get(employeeDto.getEmployeeId());
        if (user == null) {
            throw new IllegalArgumentException("employee not found: " + employeeDto.getEmployeeId());
        }

        OrderEmployee assignment = new OrderEmployee();
        assignment.setUser(user);
        assignment.setProcessedCount(employeeDto.getProcessedCount() == null ? 0 : employeeDto.getProcessedCount());
        assignment.setStepOrder(employeeDto.getStepOrder());
        assignment.setWorkStatus(EmployeeWorkStatus.PENDING);
        return assignment;
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = mapper.toDto(order);
        dto.setEmployees(mapEmployees(order));
        populateProgress(order, dto);
        return dto;
    }

    private void populateProgress(Order order, OrderDto dto) {
        dto.setProcessedCount(calculateCompletedCount(order));
        dto.setCurrentStepProcessedCount(calculateCurrentStepProcessedCount(order));

        OrderEmployee activeEmployee = findActiveEmployee(order);
        if (activeEmployee == null) {
            return;
        }

        dto.setActiveEmployeeId(activeEmployee.getUser().getId());
        dto.setActiveEmployeeName(buildFullName(activeEmployee.getUser()));
    }

    private List<EmployeeDto> mapEmployees(Order order) {
        return order.getEmployees().stream()
                .sorted(Comparator.comparing(OrderEmployee::getStepOrder))
                .map(this::toEmployeeDto)
                .toList();
    }

    private EmployeeDto toEmployeeDto(OrderEmployee assignment) {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeId(assignment.getUser().getId());
        dto.setEmployeeName(buildFullName(assignment.getUser()));
        dto.setProcessedCount(assignment.getProcessedCount());
        dto.setStepOrder(assignment.getStepOrder());
        dto.setNotes(assignment.getNotes());
        dto.setWorkStatus(assignment.getWorkStatus());
        return dto;
    }

    private void alignWorkflow(Order order) {
        List<OrderEmployee> sortedEmployees = getSortedEmployees(order);
        if (sortedEmployees.isEmpty()) {
            return;
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            sortedEmployees.forEach(employee -> employee.setWorkStatus(EmployeeWorkStatus.COMPLETED));
            return;
        }

        OrderEmployee currentEmployee = sortedEmployees.stream()
                .filter(employee -> employee.getWorkStatus() != EmployeeWorkStatus.COMPLETED)
                .findFirst()
                .orElse(null);

        if (currentEmployee == null) {
            order.setStatus(OrderStatus.COMPLETED);
            sortedEmployees.forEach(employee -> employee.setWorkStatus(EmployeeWorkStatus.COMPLETED));
            return;
        }

        if (order.getStatus() == OrderStatus.PAUSED || order.getStatus() == OrderStatus.PENDING) {
            sortedEmployees.stream()
                    .filter(employee -> employee.getWorkStatus() != EmployeeWorkStatus.COMPLETED)
                    .forEach(employee -> employee.setWorkStatus(EmployeeWorkStatus.PENDING));
            return;
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        for (OrderEmployee employee : sortedEmployees) {
            if (employee.getWorkStatus() == EmployeeWorkStatus.COMPLETED) {
                continue;
            }
            employee.setWorkStatus(employee == currentEmployee
                    ? EmployeeWorkStatus.STARTED
                    : EmployeeWorkStatus.PENDING);
        }
    }

    private List<OrderEmployee> getSortedEmployees(Order order) {
        return order.getEmployees().stream()
                .sorted(Comparator.comparing(OrderEmployee::getStepOrder))
                .toList();
    }

    private OrderEmployee findActiveEmployee(Order order) {
        return getSortedEmployees(order).stream()
                .filter(employee -> employee.getWorkStatus() == EmployeeWorkStatus.STARTED)
                .findFirst()
                .orElse(null);
    }

    private int calculateCompletedCount(Order order) {
        return getSortedEmployees(order).stream()
                .reduce((first, second) -> second)
                .map(OrderEmployee::getProcessedCount)
                .orElse(0);
    }

    private int calculateCurrentStepProcessedCount(Order order) {
        OrderEmployee activeEmployee = findActiveEmployee(order);
        if (activeEmployee != null) {
            return activeEmployee.getProcessedCount();
        }
        return calculateCompletedCount(order);
    }

    private boolean hasIncompleteEmployees(Order order) {
        return order.getEmployees().stream()
                .anyMatch(employee -> employee.getWorkStatus() != EmployeeWorkStatus.COMPLETED);
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }

    private void attachUpload(Order order, UUID uploadId) {
        if (uploadId == null) {
            return;
        }

        Upload upload = uploadService.attachToOwner(
                uploadId,
                OwnerType.ORDER,
                order.getId()
        );

        order.setUpload(upload);
        order.setImageUrl(uploadService.buildFileUrl(upload));
    }
}
