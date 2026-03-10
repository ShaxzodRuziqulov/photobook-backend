package com.example.photobook.service;

import com.example.photobook.dto.OrderDto;
import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.dto.OrderStatusTransitionDto;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.mapper.OrderMapper;
import com.example.photobook.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductCategoryService productCategoryService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final OrderStatusHistoryService orderStatusHistoryService;

    public OrderDto create(OrderDto dto) {
        validateOrder(dto);
        Order order = mapper.toEntity(dto);
        if (dto.getCategoryId() != null) {
            order.setCategory(productCategoryService.findByProductCategoryId(dto.getCategoryId()));
        }
        if (dto.getCustomerId() != null) {
            order.setCustomer(customerService.findEntityById(dto.getCustomerId()));
        }
        if (dto.getEmployeeId() != null) {
            order.setEmployee(employeeService.findEntityById(dto.getEmployeeId()));
        }
        order.setImageUrl(dto.getImageUrl());
        order.setNotes(dto.getNotes());

        return mapper.toDto(repository.save(order));
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
        if (dto.getCustomerId() != null) {
            order.setCustomer(customerService.findEntityById(dto.getCustomerId()));
        }
        order.setReceiverName(dto.getReceiverName());
        if (dto.getEmployeeId() != null) {
            order.setEmployee(employeeService.findEntityById(dto.getEmployeeId()));
        }
        order.setPageCount(dto.getPageCount());
        order.setAmount(dto.getAmount());
        order.setProcessedCount(dto.getProcessedCount());
        order.setAcceptedDate(dto.getAcceptedDate());
        order.setDeadline(dto.getDeadline());
        order.setStatus(dto.getStatus());
        order.setImageUrl(dto.getImageUrl());
        order.setNotes(dto.getNotes());

        return mapper.toDto(repository.save(order));
    }

    public OrderDto findById(UUID id) {
        Order order = findByOrderId(id);
        return mapper.toDto(order);
    }

    public List<OrderDto> findAll() {
        return mapper.toDto(repository.findAll());
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
        if (dto.getCustomerId() == null) {
            throw new IllegalArgumentException("customer_id is required");
        }
        if (dto.getEmployeeId() == null) {
            throw new IllegalArgumentException("employee_id is required");
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
}
