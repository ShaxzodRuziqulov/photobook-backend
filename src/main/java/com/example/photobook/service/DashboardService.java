package com.example.photobook.service;

import com.example.photobook.dto.DashboardAmountTrendDto;
import com.example.photobook.dto.DashboardCountDto;
import com.example.photobook.dto.DashboardSummaryDto;
import com.example.photobook.entity.Expense;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.ProductCategory;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.ExpenseRepository;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orderRepository;
    private final ExpenseRepository expenseRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public DashboardSummaryDto getSummary(LocalDate from, LocalDate to) {
        List<Order> orders = filterOrders(orderRepository.findAll(), from, to);
        List<Expense> expenses = filterExpenses(expenseRepository.findAll(), from, to);

        BigDecimal revenueTotal = orders.stream()
                .map(order -> BigDecimal.valueOf(order.getAmount() == null ? 0 : order.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expensesTotal = expenses.stream()
                .map(expense -> expense.getPrice() == null ? BigDecimal.ZERO : expense.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setOrdersTotal(orders.stream().mapToLong(this::getOrderAmount).sum());
        dto.setOrdersDone(orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .mapToLong(this::getOrderAmount)
                .sum());
        dto.setOrdersInProgress(orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.IN_PROGRESS)
                .mapToLong(this::getOrderAmount)
                .sum());
        dto.setRevenueTotal(revenueTotal);
        dto.setExpensesTotal(expensesTotal);
        dto.setProfit(revenueTotal.subtract(expensesTotal));
        return dto;
    }

    public List<DashboardCountDto> getOrdersByStatus(OrderKind type) {
        Map<OrderStatus, Long> countsByStatus = orderRepository.findAll().stream()
                .filter(order -> order.getKind() == type)
                .collect(Collectors.groupingBy(Order::getStatus,
                        () -> new EnumMap<>(OrderStatus.class),
                        Collectors.summingLong(this::getOrderAmount)));

        return Arrays.stream(OrderStatus.values())
                .map(status -> new DashboardCountDto(status.name(), countsByStatus.getOrDefault(status, 0L)))
                .toList();
    }

    public List<DashboardCountDto> getOrdersByKind() {
        Map<OrderKind, Long> countsByKind = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(Order::getKind,
                        () -> new EnumMap<>(OrderKind.class),
                        Collectors.summingLong(this::getOrderAmount)));

        return Arrays.stream(OrderKind.values())
                .map(kind -> new DashboardCountDto(kind.name(), countsByKind.getOrDefault(kind, 0L)))
                .toList();
    }

    public List<DashboardCountDto> getOrdersByCategory(OrderKind type) {
        Map<String, Long> countsByCategory = orderRepository.findAllWithDetails().stream()
                .filter(order -> order.getKind() == type)
                .filter(order -> order.getCategory() != null)
                .collect(Collectors.groupingBy(order -> order.getCategory().getName(),
                        Collectors.summingLong(this::getOrderAmount)));

        return productCategoryRepository.findByKindOrderByNameAsc(type).stream()
                .map(ProductCategory::getName)
                .map(categoryName -> new DashboardCountDto(categoryName, countsByCategory.getOrDefault(categoryName, 0L)))
                .toList();
    }

    public List<DashboardAmountTrendDto> getRevenueTrend() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getAcceptedDate() != null)
                .collect(Collectors.groupingBy(order -> YearMonth.from(order.getAcceptedDate()),
                        Collectors.mapping(order -> BigDecimal.valueOf(order.getAmount() == null ? 0 : order.getAmount()),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardAmountTrendDto(entry.getKey().toString(), entry.getValue()))
                .toList();
    }

    public List<DashboardAmountTrendDto> getExpensesTrend() {
        return expenseRepository.findAll().stream()
                .filter(expense -> expense.getExpenseDate() != null)
                .collect(Collectors.groupingBy(expense -> YearMonth.from(expense.getExpenseDate()),
                        Collectors.mapping(expense -> expense.getPrice() == null ? BigDecimal.ZERO : expense.getPrice(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardAmountTrendDto(entry.getKey().toString(), entry.getValue()))
                .toList();
    }

    private List<Order> filterOrders(List<Order> orders, LocalDate from, LocalDate to) {
        return orders.stream()
                .filter(order -> from == null || (order.getAcceptedDate() != null && !order.getAcceptedDate().isBefore(from)))
                .filter(order -> to == null || (order.getAcceptedDate() != null && !order.getAcceptedDate().isAfter(to)))
                .sorted(Comparator.comparing(Order::getAcceptedDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<Expense> filterExpenses(List<Expense> expenses, LocalDate from, LocalDate to) {
        return expenses.stream()
                .filter(expense -> from == null || (expense.getExpenseDate() != null && !expense.getExpenseDate().isBefore(from)))
                .filter(expense -> to == null || (expense.getExpenseDate() != null && !expense.getExpenseDate().isAfter(to)))
                .toList();
    }

    private long getOrderAmount(Order order) {
        return order.getAmount() == null ? 0L : order.getAmount().longValue();
    }
}
