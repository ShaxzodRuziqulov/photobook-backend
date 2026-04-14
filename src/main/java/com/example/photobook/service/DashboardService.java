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
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ExpenseRepository expenseRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public DashboardSummaryDto getSummary(LocalDate from, LocalDate to) {
        List<Order> orders = getFilteredOrders(from, to);
        List<Expense> expenses = getFilteredExpenses(from, to);

        BigDecimal revenueTotal = BigDecimal.ZERO;
        for (Order order : orders) {
            revenueTotal = revenueTotal.add(getOrderAmountBigDecimal(order));
        }

        BigDecimal expensesTotal = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            expensesTotal = expensesTotal.add(getExpenseAmount(expense));
        }

        long ordersTotal = 0;
        long ordersDone = 0;
        long ordersInProgress = 0;

        for (Order order : orders) {
            long amount = getOrderAmount(order);
            ordersTotal += amount;

            if (order.getStatus() == OrderStatus.COMPLETED) {
                ordersDone += amount;
            }

            if (order.getStatus() == OrderStatus.IN_PROGRESS) {
                ordersInProgress += amount;
            }
        }

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setOrdersTotal(ordersTotal);
        dto.setOrdersDone(ordersDone);
        dto.setOrdersInProgress(ordersInProgress);
        dto.setRevenueTotal(revenueTotal);
        dto.setExpensesTotal(expensesTotal);
        dto.setProfit(revenueTotal.subtract(expensesTotal));

        return dto;
    }

    public List<DashboardCountDto> getOrdersByStatus(OrderKind kind) {
        List<Order> orders = orderRepository.findAllWithDetails();
        Map<String, Long> result = new HashMap<>();

        for (Order order : orders) {
            if (order.getKind() != kind) {
                continue;
            }
            if (order.getCategory() == null || order.getStatus() == null) {
                continue;
            }

            String key = order.getCategory().getName() + "|" + order.getStatus().name();
            result.put(key, result.getOrDefault(key, 0L) + getOrderAmount(order));
        }

        List<DashboardCountDto> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : result.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String categoryName = parts[0];
            String statusName = parts[1];

            list.add(new DashboardCountDto(
                    entry.getValue(),
                    kind.name(),
                    statusName,
                    categoryName
            ));
        }

        list.sort(Comparator
                .comparing(DashboardCountDto::getCategory, Comparator.nullsLast(String::compareTo))
                .thenComparing(DashboardCountDto::getStatus, Comparator.nullsLast(String::compareTo)));

        return list;
    }

    public List<DashboardCountDto> getOrdersByKind() {
        Map<OrderKind, Long> counts = new EnumMap<>(OrderKind.class);

        for (Order order : orderRepository.findAll()) {
            OrderKind kind = order.getKind();
            if (kind == null) {
                continue;
            }

            counts.put(kind, counts.getOrDefault(kind, 0L) + getOrderAmount(order));
        }

        List<DashboardCountDto> list = new ArrayList<>();
        for (OrderKind kind : OrderKind.values()) {
            list.add(new DashboardCountDto(
                    counts.getOrDefault(kind, 0L),
                    kind.name(),
                    null,
                    null
            ));
        }

        return list;
    }

    public List<DashboardCountDto> getOrdersByCategory(OrderKind kind) {
        Map<String, Long> counts = new HashMap<>();

        for (Order order : orderRepository.findAllWithDetails()) {
            if (order.getKind() != kind) {
                continue;
            }
            if (order.getCategory() == null) {
                continue;
            }

            String categoryName = order.getCategory().getName();
            counts.put(categoryName, counts.getOrDefault(categoryName, 0L) + getOrderAmount(order));
        }

        List<DashboardCountDto> list = new ArrayList<>();
        List<ProductCategory> categories = productCategoryRepository.findByKindOrderByNameAsc(kind);

        for (ProductCategory category : categories) {
            String categoryName = category.getName();
            list.add(new DashboardCountDto(
                    counts.getOrDefault(categoryName, 0L),
                    kind.name(),
                    null,
                    categoryName
            ));
        }

        return list;
    }

    public List<DashboardAmountTrendDto> getRevenueTrend() {
        Map<YearMonth, BigDecimal> trend = new TreeMap<>();

        for (Order order : orderRepository.findAll()) {
            if (order.getAcceptedDate() == null) {
                continue;
            }

            YearMonth month = YearMonth.from(order.getAcceptedDate());
            BigDecimal amount = getOrderAmountBigDecimal(order);

            trend.put(month, trend.getOrDefault(month, BigDecimal.ZERO).add(amount));
        }

        List<DashboardAmountTrendDto> list = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal> entry : trend.entrySet()) {
            list.add(new DashboardAmountTrendDto(
                    entry.getKey().toString(),
                    entry.getValue()
            ));
        }

        return list;
    }

    public List<DashboardAmountTrendDto> getExpensesTrend() {
        Map<YearMonth, BigDecimal> trend = new TreeMap<>();

        for (Expense expense : expenseRepository.findAll()) {
            if (expense.getExpenseDate() == null) {
                continue;
            }

            YearMonth month = YearMonth.from(expense.getExpenseDate());
            BigDecimal amount = getExpenseAmount(expense);

            trend.put(month, trend.getOrDefault(month, BigDecimal.ZERO).add(amount));
        }

        List<DashboardAmountTrendDto> list = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal> entry : trend.entrySet()) {
            list.add(new DashboardAmountTrendDto(
                    entry.getKey().toString(),
                    entry.getValue()
            ));
        }

        return list;
    }

    private List<Order> getFilteredOrders(LocalDate from, LocalDate to) {
        List<Order> result = new ArrayList<>();

        for (Order order : orderRepository.findAll()) {
            LocalDate date = order.getAcceptedDate();
            if (date == null) {
                continue;
            }
            if (from != null && date.isBefore(from)) {
                continue;
            }
            if (to != null && date.isAfter(to)) {
                continue;
            }

            result.add(order);
        }

        return result;
    }

    private List<Expense> getFilteredExpenses(LocalDate from, LocalDate to) {
        List<Expense> result = new ArrayList<>();

        for (Expense expense : expenseRepository.findAll()) {
            LocalDate date = expense.getExpenseDate();
            if (date == null) {
                continue;
            }
            if (from != null && date.isBefore(from)) {
                continue;
            }
            if (to != null && date.isAfter(to)) {
                continue;
            }

            result.add(expense);
        }

        return result;
    }

    private long getOrderAmount(Order order) {
        return order.getAmount() == null ? 0L : order.getAmount().longValue();
    }

    private BigDecimal getOrderAmountBigDecimal(Order order) {
        return order.getAmount() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(order.getAmount());
    }

    private BigDecimal getExpenseAmount(Expense expense) {
        return expense.getPrice() == null ? BigDecimal.ZERO : expense.getPrice();
    }
}