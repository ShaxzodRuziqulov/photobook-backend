package com.example.photobook.service;

import com.example.photobook.dto.OrderDto;
import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.dto.OrderStatusTransitionDto;
import com.example.photobook.dto.request.OrderPagingRequest;
import com.example.photobook.entity.Customer;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.Upload;
import com.example.photobook.entity.User;
import com.example.photobook.entity.enumirated.OwnerType;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.mapper.OrderMapper;
import com.example.photobook.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductCategoryService productCategoryService;
    private final CustomerService customerService;
    private final UserService userService;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final UploadService uploadService;

    public OrderDto create(OrderDto dto) {
        validateOrder(dto);
        Order order = mapper.toEntity(dto);
        if (dto.getCategoryId() != null) {
            order.setCategory(productCategoryService.findByProductCategoryId(dto.getCategoryId()));
        }
        order.setCustomer(resolveCustomer(dto));
        order.setEmployees(resolveEmployees(dto.getEmployeeIds()));
        order.setImageUrl(dto.getImageUrl());
        order.setNotes(dto.getNotes());
        Order savedOrder = repository.save(order);
        attachUploadIfPresent(savedOrder, dto.getUploadId());
        return mapper.toDto(savedOrder);
    }

    public OrderDto update(UUID id, OrderDto dto) {
        validateOrder(dto);
        Order order = findByOrderId(id);
        order.setKind(dto.getKind());
        if (dto.getCategoryId() != null) {
            order.setCategory(productCategoryService.findByProductCategoryId(dto.getCategoryId()));
        }
        order.setOrderName(dto.getOrderName());
        order.setItemType(dto.getItemType());
        order.setCustomer(resolveCustomer(dto));
        order.setReceiverName(dto.getReceiverName());
        order.setEmployees(resolveEmployees(dto.getEmployeeIds()));
        order.setPageCount(dto.getPageCount());
        order.setAmount(dto.getAmount());
        order.setProcessedCount(dto.getProcessedCount());
        order.setAcceptedDate(dto.getAcceptedDate());
        order.setDeadline(dto.getDeadline());
        order.setStatus(dto.getStatus());
        order.setImageUrl(dto.getImageUrl());
        order.setNotes(dto.getNotes());
        Order savedOrder = repository.save(order);
        attachUploadIfPresent(savedOrder, dto.getUploadId());
        return mapper.toDto(savedOrder);
    }

    public OrderDto findById(UUID id) {
        Order order = findByOrderId(id);
        return mapper.toDto(order);
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
                pageable).map(mapper::toDto);
    }

    public void delete(UUID id) {
        Order order = findByOrderId(id);
        repository.delete(order);
    }

    public OrderDto changeStatus(UUID id, OrderStatusTransitionDto dto, UUID changedById) {
        if (dto.getToStatus() == null) {
            throw new IllegalArgumentException("toStatus is required");
        }

        Order order = findByOrderId(id);
        OrderStatus fromStatus = order.getStatus();
        OrderStatus toStatus = dto.getToStatus();

        if (fromStatus == toStatus) {
            return mapper.toDto(order);
        }

        if (!isValidTransition(fromStatus, toStatus)) {
            throw new IllegalArgumentException("Invalid order status transition");
        }

        order.setStatus(toStatus);
        Order savedOrder = repository.save(order);

        OrderStatusHistoryDto historyDto = new OrderStatusHistoryDto();
        historyDto.setOrderId(savedOrder.getId());
        historyDto.setFromStatus(fromStatus);
        historyDto.setToStatus(toStatus);
        historyDto.setChangedById(changedById);
        historyDto.setChangedAt(LocalDateTime.now());
        orderStatusHistoryService.create(historyDto);

        return mapper.toDto(savedOrder);
    }

    public List<OrderStatusHistoryDto> getStatusHistory(UUID orderId) {
        return orderStatusHistoryService.findAllByOrderId(orderId);
    }

    public Order findByOrderId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("order not found"));
    }

    private boolean isValidTransition(OrderStatus fromStatus, OrderStatus toStatus) {
        Map<OrderStatus, Set<OrderStatus>> transitions = Map.of(
                OrderStatus.PENDING, Set.of(OrderStatus.IN_PROGRESS),
                OrderStatus.IN_PROGRESS, Set.of(OrderStatus.PENDING, OrderStatus.COMPLETED),
                OrderStatus.COMPLETED, Set.of()
        );
        return transitions.getOrDefault(fromStatus, Set.of()).contains(toStatus);
    }

    private void validateOrder(OrderDto dto) {
        if (dto.getOrderName() == null || dto.getOrderName().isBlank()) {
            throw new IllegalArgumentException("order_name is required");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("category_id is required");
        }
        if (dto.getCustomerId() == null && (dto.getCustomerName() == null || dto.getCustomerName().isBlank())) {
            throw new IllegalArgumentException("customer_id or customer_name is required");
        }
        if (dto.getEmployeeIds() == null || dto.getEmployeeIds().isEmpty()) {
            throw new IllegalArgumentException("employee_ids is required");
        }
        if (dto.getEmployeeIds().stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("employee_ids contains invalid value");
        }
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        if (dto.getProcessedCount() == null || dto.getProcessedCount() < 0) {
            throw new IllegalArgumentException("processed_count must be greater than or equal to 0");
        }
        if (dto.getProcessedCount() > dto.getAmount()) {
            throw new IllegalArgumentException("processed_count cannot be greater than amount");
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
    }

    private Set<User> resolveEmployees(List<UUID> employeeIds) {
        return employeeIds.stream()
                .distinct()
                .map(userService::findByUserId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Customer resolveCustomer(OrderDto dto) {
        if (dto.getCustomerId() != null) {
            return customerService.findEntityById(dto.getCustomerId());
        }
        return customerService.createForOrder(dto.getCustomerName().trim());
    }

    private void attachUploadIfPresent(Order order, UUID uploadId) {
        if (uploadId == null) {
            return;
        }
        Upload upload = uploadService.attachToOwner(uploadId, OwnerType.ORDER, order.getId());
        order.setUpload(upload);
        order.setImageUrl(uploadService.buildFileUrl(upload));
        repository.save(order);
    }
}
