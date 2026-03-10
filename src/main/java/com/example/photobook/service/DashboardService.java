package com.example.photobook.service;

import com.example.photobook.dto.DashboardAmountTrendDto;
import com.example.photobook.dto.DashboardCountDto;
import com.example.photobook.dto.DashboardSummaryDto;
import com.example.photobook.entity.Expense;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.ExpenseRepository;
import com.example.photobook.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orderRepository;
    private final ExpenseRepository expenseRepository;

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
        dto.setOrdersTotal(orders.size());
        dto.setOrdersDone(orders.stream().filter(order -> order.getStatus() == OrderStatus.COMPLETED).count());
        dto.setOrdersInProgress(orders.stream().filter(order -> order.getStatus() == OrderStatus.IN_PROGRESS).count());
        dto.setRevenueTotal(revenueTotal);
        dto.setExpensesTotal(expensesTotal);
        dto.setProfit(revenueTotal.subtract(expensesTotal));
        return dto;
    }

    public List<DashboardCountDto> getOrdersByStatus() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(order -> order.getStatus().name(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardCountDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<DashboardCountDto> getOrdersByKind() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(order -> order.getKind().name(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardCountDto(entry.getKey(), entry.getValue()))
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
}
