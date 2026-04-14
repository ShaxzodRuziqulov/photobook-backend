package com.example.photobook.service;

import com.example.photobook.dto.DashboardCountDto;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.ProductCategory;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.ExpenseRepository;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.repository.ProductCategoryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
    private final ProductCategoryRepository productCategoryRepository = mock(ProductCategoryRepository.class);

    private final DashboardService dashboardService =
            new DashboardService(orderRepository, expenseRepository, productCategoryRepository);

    @Test
    void shouldSumOrderAmountsByCategory() {
        ProductCategory albumA3 = category("A3 albom", OrderKind.ALBUM);
        ProductCategory albumMini = category("Kichik albom", OrderKind.ALBUM);

        when(orderRepository.findAllWithDetails()).thenReturn(List.of(
                order(OrderKind.ALBUM, OrderStatus.COMPLETED, albumA3, 200),
                order(OrderKind.ALBUM, OrderStatus.IN_PROGRESS, albumA3, 100),
                order(OrderKind.ALBUM, OrderStatus.COMPLETED, albumMini, 50)
        ));
        when(productCategoryRepository.findByKindOrderByNameAsc(OrderKind.ALBUM))
                .thenReturn(List.of(albumA3, albumMini));

        List<DashboardCountDto> result = dashboardService.getOrdersByCategory(OrderKind.ALBUM);

        assertEquals(2, result.size());
        assertEquals("A3 albom", result.get(0).getKey());
        assertEquals(300L, result.get(0).getCount());
        assertEquals("Kichik albom", result.get(1).getKey());
        assertEquals(50L, result.get(1).getCount());
    }

    @Test
    void shouldSumOrderAmountsByStatusAndKind() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order(OrderKind.ALBUM, OrderStatus.COMPLETED, category("A3 albom", OrderKind.ALBUM), 200),
                order(OrderKind.ALBUM, OrderStatus.IN_PROGRESS, category("A3 albom", OrderKind.ALBUM), 100),
                order(OrderKind.VIGNETTE, OrderStatus.COMPLETED, category("Bitiruvchi oq", OrderKind.VIGNETTE), 40)
        ));

        List<DashboardCountDto> byStatus = dashboardService.getOrdersByStatus(OrderKind.ALBUM);
        List<DashboardCountDto> byKind = dashboardService.getOrdersByKind();

        assertEquals(100L, countFor("IN_PROGRESS", byStatus));
        assertEquals(200L, countFor("COMPLETED", byStatus));
        assertEquals(300L, countFor("ALBUM", byKind));
        assertEquals(40L, countFor("VIGNETTE", byKind));
    }

    private static long countFor(String key, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> item.getKey().equals(key))
                .findFirst()
                .orElseThrow()
                .getCount();
    }

    private static Order order(OrderKind kind, OrderStatus status, ProductCategory category, Integer amount) {
        Order order = new Order();
        order.setKind(kind);
        order.setStatus(status);
        order.setCategory(category);
        order.setAmount(amount);
        return order;
    }

    private static ProductCategory category(String name, OrderKind kind) {
        ProductCategory category = new ProductCategory();
        category.setName(name);
        category.setKind(kind);
        return category;
    }
}
