package com.example.photobook.service;

import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderStatusHistory;
import com.example.photobook.entity.User;
import com.example.photobook.mapper.OrderStatusHistoryMapper;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryService {
    private final OrderStatusHistoryRepository repository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryMapper mapper;
    private final UserService userService;

    public OrderStatusHistoryDto create(OrderStatusHistoryDto dto) {
        OrderStatusHistory orderStatusHistory = mapper.toEntity(dto);
        orderStatusHistory.setOrder(findOrderById(dto.getOrderId()));
        orderStatusHistory.setChangedBy(resolveChangedBy(dto));
        orderStatusHistory.setChangedAt(dto.getChangedAt() != null ? dto.getChangedAt() : LocalDateTime.now());

        return mapper.toDto(repository.save(orderStatusHistory));
    }

    public OrderStatusHistoryDto update(UUID id, OrderStatusHistoryDto dto) {
        OrderStatusHistory orderStatusHistory = findByOrderStatusHistoryId(id);
        orderStatusHistory.setOrder(findOrderById(dto.getOrderId()));
        orderStatusHistory.setFromStatus(dto.getFromStatus());
        orderStatusHistory.setToStatus(dto.getToStatus());
        orderStatusHistory.setChangedBy(resolveChangedBy(dto));
        orderStatusHistory.setChangedAt(dto.getChangedAt() != null ? dto.getChangedAt() : orderStatusHistory.getChangedAt());

        return mapper.toDto(repository.save(orderStatusHistory));
    }

    public OrderStatusHistoryDto findById(UUID id) {
        OrderStatusHistory orderStatusHistory = findByOrderStatusHistoryId(id);
        return mapper.toDto(orderStatusHistory);
    }

    public List<OrderStatusHistoryDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public List<OrderStatusHistoryDto> findAllByOrderId(UUID orderId) {
        findOrderById(orderId);
        return mapper.toDto(repository.findByOrderIdOrderByChangedAtAsc(orderId));
    }

    public void delete(UUID id) {
        OrderStatusHistory order = findByOrderStatusHistoryId(id);
        repository.delete(order);
    }

    public OrderStatusHistory findByOrderStatusHistoryId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order Status History not found"));
    }

    private User resolveChangedBy(OrderStatusHistoryDto dto) {
        if (dto.getChangedById() == null) {
            throw new IllegalArgumentException("changedById is required");
        }
        return userService.findByUserId(dto.getChangedById());
    }

    private Order findOrderById(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
    }
}
