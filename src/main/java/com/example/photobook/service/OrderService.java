package com.example.photobook.service;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.dto.OrderDto;
import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.dto.OrderStatusTransitionDto;
import com.example.photobook.dto.request.OrderPagingRequest;
import com.example.photobook.entity.Customer;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.Upload;
import com.example.photobook.entity.User;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.entity.enumirated.OwnerType;
import com.example.photobook.mapper.OrderMapper;
import com.example.photobook.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    public OrderDto create(OrderDto dto) {
        validateOrder(dto);

        Order order = mapper.toEntity(dto);
        fillOrderFields(order, dto);

        Order saved = repository.save(order);
        attachUpload(saved, dto.getUploadId());

        return toDto(saved);
    }

    public OrderDto update(UUID id, OrderDto dto) {
        validateOrder(dto);

        Order order = findByOrderId(id);
        fillOrderFields(order, dto);

        Order saved = repository.save(order);
        attachUpload(saved, dto.getUploadId());

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

        order.setStatus(to);

        Order saved = repository.save(order);

        OrderStatusHistoryDto historyDto = new OrderStatusHistoryDto();
        historyDto.setOrderId(saved.getId());
        historyDto.setFromStatus(from);
        historyDto.setToStatus(to);
        historyDto.setChangedById(changedById);
        historyDto.setChangedAt(LocalDateTime.now());
        historyService.create(historyDto);

        return toDto(saved);
    }

    public List<OrderStatusHistoryDto> getStatusHistory(UUID orderId) {
        return historyService.findAllByOrderId(orderId);
    }

    public Order findByOrderId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
    }

    private void fillOrderFields(Order order, OrderDto dto) {
        order.setKind(dto.getKind());
        order.setOrderName(dto.getOrderName());
        order.setItemType(normalize(dto.getItemType()));
        order.setReceiverName(dto.getReceiverName().trim());
        order.setPageCount(dto.getPageCount());
        order.setAmount(dto.getAmount());
        order.setAcceptedDate(dto.getAcceptedDate());
        order.setDeadline(dto.getDeadline());
        order.setStatus(dto.getStatus());
        order.setNotes(normalize(dto.getNotes()));
        order.setImageUrl(normalize(dto.getImageUrl()));
        order.setCustomer(resolveCustomer(dto));
        order.setCategory(productCategoryService.findByProductCategoryId(dto.getCategoryId()));
        order.replaceEmployees(resolveEmployees(dto.getEmployees()));
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        Map<OrderStatus, Set<OrderStatus>> transitions = Map.of(
                OrderStatus.PENDING, Set.of(OrderStatus.IN_PROGRESS),
                OrderStatus.IN_PROGRESS, Set.of(OrderStatus.PENDING, OrderStatus.COMPLETED),
                OrderStatus.COMPLETED, Set.of()
        );

        return transitions
                .getOrDefault(from, Set.of())
                .contains(to);
    }

    private void validateOrder(OrderDto dto) {
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
        if (dto.getEmployees() == null || dto.getEmployees().isEmpty()) {
            throw new IllegalArgumentException("order must have at least one employee");
        }
        if (dto.getEmployees().stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("employees contains invalid value");
        }
        if (dto.getEmployees().stream().map(EmployeeDto::getEmployeeId).anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("employees.employeeId is required");
        }
        long distinctEmployeeCount = dto.getEmployees().stream()
                .map(EmployeeDto::getEmployeeId)
                .distinct()
                .count();
        if (distinctEmployeeCount != dto.getEmployees().size()) {
            throw new IllegalArgumentException("employees must be unique per order");
        }
        if (dto.getEmployees().stream().map(EmployeeDto::getProcessedCount).anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("employees.processedCount is required");
        }
        if (dto.getEmployees().stream().map(EmployeeDto::getProcessedCount).anyMatch(count -> count < 0)) {
            throw new IllegalArgumentException("employees.processedCount must be >= 0");
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

    private OrderEmployee toOrderEmployee(EmployeeDto employeeDto, Map<UUID, User> usersById) {
        User user = usersById.get(employeeDto.getEmployeeId());
        if (user == null) {
            throw new IllegalArgumentException("employee not found: " + employeeDto.getEmployeeId());
        }

        OrderEmployee assignment = new OrderEmployee();
        assignment.setUser(user);
        assignment.setProcessedCount(employeeDto.getProcessedCount());
        return assignment;
    }

    private Customer resolveCustomer(OrderDto dto) {
        if (dto.getCustomerId() != null) {
            return customerService.findEntityById(dto.getCustomerId());
        }
        return customerService.createForOrder(dto.getCustomerName().trim());
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = mapper.toDto(order);
        dto.setEmployees(mapEmployees(order));
        return dto;
    }

    private List<EmployeeDto> mapEmployees(Order order) {
        return order.getEmployees().stream()
                .sorted(Comparator.comparing(assignment -> assignment.getUser().getId()))
                .map(this::toEmployeeDto)
                .toList();
    }

    private EmployeeDto toEmployeeDto(OrderEmployee assignment) {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeId(assignment.getUser().getId());
        dto.setEmployeeName(buildFullName(assignment.getUser()));
        dto.setProcessedCount(assignment.getProcessedCount());
        return dto;
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
