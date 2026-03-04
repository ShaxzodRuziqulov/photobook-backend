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
        OrderStatusHistory Order = mapper.toEntity(dto);

        return mapper.toDto(repository.save(Order));
    }

    public OrderStatusHistoryDto update(UUID id, OrderStatusHistoryDto dto) {
        OrderStatusHistory order = findByOrderStatusHistoryId(id);
        order.setId(dto.getId());
        order.setOrder(orderService.findByOrderId(dto.getOrderId()));
        order.setFromStatus(dto.getFromStatus());
        order.setToStatus(dto.getToStatus());
        order.setChangedBy(userService.findByUserId(dto.getChangedById()));
        order.setChangedAt(dto.getChangedAt());

        return mapper.toDto(repository.save(order));
    }

    public OrderStatusHistoryDto findById(UUID id) {
        OrderStatusHistory Order = findByOrderStatusHistoryId(id);
        return mapper.toDto(Order);
    }

    public List<OrderStatusHistoryDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        OrderStatusHistory order = findByOrderStatusHistoryId(id);
        repository.delete(order);
    }

    public OrderStatusHistory findByOrderStatusHistoryId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order Status History not found"));
    }
}
