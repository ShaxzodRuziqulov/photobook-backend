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
        assertEquals(300L, countForCategory("A3 albom", result));
        assertEquals(50L, countForCategory("Kichik albom", result));
        assertEquals("ALBUM", kindForCategory("A3 albom", result));
        assertEquals(null, statusForCategory("A3 albom", result));
    }

    @Test
    void shouldSumOrderAmountsByStatusAndKind() {
        ProductCategory albumA3 = category("A3 albom", OrderKind.ALBUM);
        ProductCategory albumMini = category("Kichik albom", OrderKind.ALBUM);
        ProductCategory vignetteWhite = category("Bitiruvchi oq", OrderKind.VIGNETTE);

        when(orderRepository.findAll()).thenReturn(List.of(
                order(OrderKind.ALBUM, OrderStatus.COMPLETED, albumA3, 200),
                order(OrderKind.ALBUM, OrderStatus.IN_PROGRESS, albumA3, 50),
                order(OrderKind.ALBUM, OrderStatus.COMPLETED, albumMini, 30),
                order(OrderKind.VIGNETTE, OrderStatus.COMPLETED, vignetteWhite, 40)
        ));
        when(orderRepository.findAllWithDetails()).thenReturn(List.of(
                order(OrderKind.ALBUM, OrderStatus.COMPLETED, category("A3 albom", OrderKind.ALBUM), 200),
                order(OrderKind.ALBUM, OrderStatus.IN_PROGRESS, category("A3 albom", OrderKind.ALBUM), 50),
                order(OrderKind.ALBUM, OrderStatus.COMPLETED, category("Kichik albom", OrderKind.ALBUM), 30),
                order(OrderKind.VIGNETTE, OrderStatus.COMPLETED, category("Bitiruvchi oq", OrderKind.VIGNETTE), 40)
        ));

        List<DashboardCountDto> byStatus = dashboardService.getOrdersByStatus(OrderKind.ALBUM);
        List<DashboardCountDto> byKind = dashboardService.getOrdersByKind();

        assertEquals(3, byStatus.size());
        assertEquals(200L, countForStatus("COMPLETED", "A3 albom", byStatus));
        assertEquals(50L, countForStatus("IN_PROGRESS", "A3 albom", byStatus));
        assertEquals(30L, countForStatus("COMPLETED", "Kichik albom", byStatus));
        assertEquals(280L, countForKind("ALBUM", byKind));
        assertEquals(40L, countForKind("VIGNETTE", byKind));
        assertEquals("ALBUM", kindForStatus("IN_PROGRESS", "A3 albom", byStatus));
        assertEquals("IN_PROGRESS", statusForStatus("IN_PROGRESS", "A3 albom", byStatus));
        assertEquals("A3 albom", categoryForStatus("IN_PROGRESS", "A3 albom", byStatus));
        assertEquals("ALBUM", kindForKind("ALBUM", byKind));
        assertEquals(null, statusForKind("ALBUM", byKind));
        assertEquals(null, categoryForKind("ALBUM", byKind));
    }

    private static long countForKind(String kind, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getKind(), kind))
                .findFirst()
                .orElseThrow()
                .getCount();
    }

    private static long countForStatus(String status, String category, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getStatus(), status))
                .filter(item -> java.util.Objects.equals(item.getCategory(), category))
                .findFirst()
                .orElseThrow()
                .getCount();
    }

    private static long countForCategory(String category, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getCategory(), category))
                .findFirst()
                .orElseThrow()
                .getCount();
    }

    private static String categoryForStatus(String status, String category, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getStatus(), status))
                .filter(item -> java.util.Objects.equals(item.getCategory(), category))
                .findFirst()
                .orElseThrow()
                .getCategory();
    }

    private static String categoryForKind(String kind, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getKind(), kind))
                .findFirst()
                .orElseThrow()
                .getCategory();
    }

    private static String kindForStatus(String status, String category, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getStatus(), status))
                .filter(item -> java.util.Objects.equals(item.getCategory(), category))
                .findFirst()
                .orElseThrow()
                .getKind();
    }

    private static String kindForKind(String kind, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getKind(), kind))
                .findFirst()
                .orElseThrow()
                .getKind();
    }

    private static String kindForCategory(String category, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getCategory(), category))
                .findFirst()
                .orElseThrow()
                .getKind();
    }

    private static String statusForStatus(String status, String category, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getStatus(), status))
                .filter(item -> java.util.Objects.equals(item.getCategory(), category))
                .findFirst()
                .orElseThrow()
                .getStatus();
    }

    private static String statusForKind(String kind, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getKind(), kind))
                .findFirst()
                .orElseThrow()
                .getStatus();
    }

    private static String statusForCategory(String category, List<DashboardCountDto> items) {
        return items.stream()
                .filter(item -> java.util.Objects.equals(item.getCategory(), category))
                .findFirst()
                .orElseThrow()
                .getStatus();
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
