package com.example.photobook.service;

import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.entity.OrderStatusHistory;
import com.example.photobook.mapper.OrderStatusHistoryMapper;
import com.example.photobook.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryService {
    private final OrderStatusHistoryRepository repository;
    private final OrderStatusHistoryMapper mapper;
    private final OrderService orderService;
    private final UserService userService;

    public OrderStatusHistoryDto create(OrderStatusHistoryDto dto) {
        OrderStatusHistory order = mapper.toEntity(dto);
        if (dto.getOrderId() != null) {
            order.setOrder(orderService.findByOrderId(dto.getOrderId()));
        }
        if (dto.getChangedById() != null) {
            order.setChangedBy(userService.findByUserId(dto.getChangedById()));
        }

        return mapper.toDto(repository.save(order));
    }

    public OrderStatusHistoryDto update(UUID id, OrderStatusHistoryDto dto) {
        OrderStatusHistory order = findByOrderId(id);
        if (dto.getOrderId() != null) {
            order.setOrder(orderService.findByOrderId(dto.getOrderId()));
        }
        order.setFromStatus(dto.getFromStatus());
        order.setToStatus(dto.getToStatus());
        if (dto.getChangedById() != null) {
            order.setChangedBy(userService.findByUserId(dto.getChangedById()));
        }
        order.setChangedAt(dto.getChangedAt());

        return mapper.toDto(repository.save(order));
    }

    public OrderStatusHistoryDto findById(UUID id) {
        OrderStatusHistory order = findByOrderId(id);
        return mapper.toDto(order);
    }

    public List<OrderStatusHistoryDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        OrderStatusHistory order = findByOrderId(id);
        repository.delete(order);
    }

    public OrderStatusHistory findByOrderId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("order status history not found"));
    }
}
