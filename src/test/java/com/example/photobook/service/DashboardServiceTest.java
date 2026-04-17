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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ProductCategoryRepository productCategoryRepository = mock(ProductCategoryRepository.class);

    private final DashboardService dashboardService =
            new DashboardService(orderRepository, productCategoryRepository);

    @Test
    void shouldReturnAllKindsWithZeroCountsForMissingValues() {
        when(orderRepository.countOrdersByKind()).thenReturn(List.of(
                kindCount(OrderKind.ALBUM, 56),
                kindCount(OrderKind.PICTURE, 7)
        ));

        List<DashboardKeyCountDto> result = dashboardService.getOrdersByKind();

        assertEquals(3, result.size());
        assertEquals(56L, countForKey("ALBUM", result));
        assertEquals(0L, countForKey("VIGNETTE", result));
        assertEquals(7L, countForKey("PICTURE", result));
    }

    @Test
    void shouldReturnAllStatusesWithZeroCountsForMissingValues() {
        when(orderRepository.countOrdersByStatus(OrderKind.ALBUM)).thenReturn(List.of(
                statusCount(OrderStatus.PENDING, 3),
                statusCount(OrderStatus.IN_PROGRESS, 40),
                statusCount(OrderStatus.COMPLETED, 12)
        ));

        List<DashboardKeyCountDto> result = dashboardService.getOrdersByStatus(OrderKind.ALBUM);

        assertEquals(OrderStatus.values().length, result.size());
        assertEquals(3L, countForKey("PENDING", result));
        assertEquals(40L, countForKey("IN_PROGRESS", result));
        assertEquals(0L, countForKey("PAUSED", result));
        assertEquals(12L, countForKey("COMPLETED", result));
        assertEquals(0L, countForKey("CANCELLED", result));
    }

    @Test
    void shouldMapCategoryAggregationRows() {
        UUID categoryId = UUID.randomUUID();

        when(productCategoryRepository.countOrdersByCategory(OrderKind.ALBUM)).thenReturn(List.of(
                categoryCount(categoryId, "A3 albom", 2)
        ));

        List<DashboardCategoryCountDto> result = dashboardService.getOrdersByCategory(OrderKind.ALBUM);

        assertEquals(1, result.size());
        assertEquals(categoryId, result.get(0).categoryId());
        assertEquals("A3 albom", result.get(0).categoryName());
        assertEquals(2L, result.get(0).count());
    }

    private static long countForKey(String key, List<DashboardKeyCountDto> items) {
        return items.stream()
                .filter(item -> key.equals(item.key()))
                .findFirst()
                .orElseThrow()
                .count();
    }

    private static OrderKindCountProjection kindCount(OrderKind kind, long count) {
        return new OrderKindCountProjection() {
            @Override
            public OrderKind getKind() {
                return kind;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    private static OrderStatusCountProjection statusCount(OrderStatus status, long count) {
        return new OrderStatusCountProjection() {
            @Override
            public OrderStatus getStatus() {
                return status;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    private static OrderCategoryCountProjection categoryCount(UUID categoryId, String categoryName, long count) {
        return new OrderCategoryCountProjection() {
            @Override
            public UUID getCategoryId() {
                return categoryId;
            }

            @Override
            public String getCategoryName() {
                return categoryName;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }
}
