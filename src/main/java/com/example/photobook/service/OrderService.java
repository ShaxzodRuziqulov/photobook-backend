package com.example.photobook.service;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.dto.OrderDto;
import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.dto.OrderStatusTransitionDto;
import com.example.photobook.dto.request.OrderPagingRequest;
import com.example.photobook.entity.Customer;
import com.example.photobook.entity.Order;
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
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

        return mapper.toDto(saved);
    }

    public OrderDto update(UUID id, OrderDto dto) {
        validateOrder(dto);

        Order order = findByOrderId(id);
        fillOrderFields(order, dto);

        Order saved = repository.save(order);
        attachUpload(saved, dto.getUploadId());

        return mapper.toDto(saved);
    }

    private void fillOrderFields(Order order, OrderDto dto) {

        order.setKind(dto.getKind());
        order.setOrderName(dto.getOrderName());
        order.setItemType(dto.getItemType());
        order.setReceiverName(dto.getReceiverName());
        order.setPageCount(dto.getPageCount());
        order.setAmount(dto.getAmount());
        order.setProcessedCount(dto.getProcessedCount());
        order.setAcceptedDate(dto.getAcceptedDate());
        order.setDeadline(dto.getDeadline());
        order.setStatus(dto.getStatus());
        order.setNotes(dto.getNotes());
        order.setImageUrl(dto.getImageUrl());

        order.setCustomer(resolveCustomer(dto));
        order.setEmployees(resolveEmployees(dto.getEmployees()));

        if (dto.getCategoryId() != null) {
            order.setCategory(productCategoryService
                    .findByProductCategoryId(dto.getCategoryId()));
        }
    }

    public OrderDto findById(UUID id) {
        return mapper.toDto(findByOrderId(id));
    }

    public List<OrderDto> findAll() {
        return mapper.toDto(repository.findAll());
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
        ).map(mapper::toDto);
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
            return mapper.toDto(order);
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

        return mapper.toDto(saved);
    }

    public List<OrderStatusHistoryDto> getStatusHistory(UUID orderId) {
        return historyService.findAllByOrderId(orderId);
    }

    public Order findByOrderId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
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

        if (dto.getKind() == null)
            throw new IllegalArgumentException("kind is required");

        if (dto.getOrderName() == null || dto.getOrderName().isBlank())
            throw new IllegalArgumentException("order_name is required");

        if (dto.getCategoryId() == null)
            throw new IllegalArgumentException("category_id is required");

        if (dto.getCustomerId() == null &&
                (dto.getCustomerName() == null || dto.getCustomerName().isBlank()))
            throw new IllegalArgumentException("customer_id or customer_name is required");

        if (dto.getReceiverName() == null || dto.getReceiverName().isBlank())
            throw new IllegalArgumentException("receiver_name is required");

        if (dto.getEmployees() == null || dto.getEmployees().isEmpty())
            throw new IllegalArgumentException("employees is required");

        if (dto.getEmployees().stream().anyMatch(java.util.Objects::isNull))
            throw new IllegalArgumentException("employees contains invalid value");

        if (dto.getEmployees().stream()
                .map(EmployeeDto::getEmployeeIds)
                .anyMatch(java.util.Objects::isNull))
            throw new IllegalArgumentException("employees.employeeIds contains invalid value");

        if (dto.getPageCount() == null || dto.getPageCount() < 0)
            throw new IllegalArgumentException("page_count must be >= 0");

        if (dto.getAmount() == null || dto.getAmount() <= 0)
            throw new IllegalArgumentException("amount must be > 0");

        if (dto.getProcessedCount() == null || dto.getProcessedCount() < 0)
            throw new IllegalArgumentException("processed_count must be >= 0");

        if (dto.getProcessedCount() > dto.getAmount())
            throw new IllegalArgumentException("processed_count cannot be greater than amount");

        if (dto.getAcceptedDate() == null)
            throw new IllegalArgumentException("accepted_date is required");

        if (dto.getDeadline() == null)
            throw new IllegalArgumentException("deadline is required");

        if (dto.getDeadline().isBefore(dto.getAcceptedDate()))
            throw new IllegalArgumentException("deadline cannot be earlier than accepted_date");

        if (dto.getStatus() == null)
            throw new IllegalArgumentException("status is required");
    }

    private Set<User> resolveEmployees(List<EmployeeDto> employees) {

        return employees.stream()
                .map(EmployeeDto::getEmployeeIds)
                .distinct()
                .map(userService::findByUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Customer resolveCustomer(OrderDto dto) {

        if (dto.getCustomerId() != null) {
            return customerService.findEntityById(dto.getCustomerId());
        }

        return customerService.createForOrder(dto.getCustomerName().trim());
    }

    private void attachUpload(Order order, UUID uploadId) {

        if (uploadId == null)
            return;

        Upload upload = uploadService.attachToOwner(
                uploadId,
                OwnerType.ORDER,
                order.getId()
        );

        order.setUpload(upload);
        order.setImageUrl(uploadService.buildFileUrl(upload));
    }
}
