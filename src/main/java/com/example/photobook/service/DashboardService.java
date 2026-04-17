package com.example.photobook.service;

import com.example.photobook.dto.DashboardCategoryCountDto;
import com.example.photobook.dto.DashboardKeyCountDto;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.projection.OrderCategoryCountProjection;
import com.example.photobook.projection.OrderKindCountProjection;
import com.example.photobook.projection.OrderStatusCountProjection;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for dashboard statistics backed by database aggregation queries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public List<DashboardKeyCountDto> getOrdersByKind() {
        Map<OrderKind, Long> counts = new EnumMap<>(OrderKind.class);
        for (OrderKind kind : OrderKind.values()) {
            counts.put(kind, 0L);
        }

        for (OrderKindCountProjection row : orderRepository.countOrdersByKind()) {
            if (row.getKind() != null) {
                counts.put(row.getKind(), row.getCount());
            }
        }

        return Arrays.stream(OrderKind.values())
                .map(kind -> new DashboardKeyCountDto(kind.name(), counts.get(kind)))
                .toList();
    }

    public List<DashboardKeyCountDto> getOrdersByStatus(OrderKind kind) {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, 0L);
        }

        for (OrderStatusCountProjection row : orderRepository.countOrdersByStatus(kind)) {
            if (row.getStatus() != null) {
                counts.put(row.getStatus(), row.getCount());
            }
        }

        return Arrays.stream(OrderStatus.values())
                .map(status -> new DashboardKeyCountDto(status.name(), counts.get(status)))
                .toList();
    }

    public List<DashboardCategoryCountDto> getOrdersByCategory(OrderKind kind) {
        return productCategoryRepository.countOrdersByCategory(kind)
                .stream()
                .map(this::toCategoryDto)
                .toList();
    }

    private DashboardCategoryCountDto toCategoryDto(OrderCategoryCountProjection row) {
        return new DashboardCategoryCountDto(
                row.getCategoryId(),
                row.getCategoryName(),
                row.getTotalAmount()
        );
    }
}
