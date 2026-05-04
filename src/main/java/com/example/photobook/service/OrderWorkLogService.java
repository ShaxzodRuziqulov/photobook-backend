package com.example.photobook.service;

import com.example.photobook.dto.EmployeeOrderSummaryDto;
import com.example.photobook.dto.WorkLogResponseDto;
import com.example.photobook.entity.OrderWorkLog;
import com.example.photobook.entity.User;
import com.example.photobook.projection.MonthlyWorkSummaryProjection;
import com.example.photobook.repository.OrderWorkLogRepository;
import com.example.photobook.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderWorkLogService {
    private final OrderWorkLogRepository repository;
    private final CurrentUserService currentUserService;

    public void logProgress(UUID orderId, UUID employeeId, int stepOrder, int previousCount, int newCount, String notes) {
        int delta = newCount - previousCount;
        if (delta <= 0) return;

        OrderWorkLog log = new OrderWorkLog();
        log.setOrderId(orderId);
        log.setEmployeeId(employeeId);
        log.setStepOrder(stepOrder);
        log.setDelta(delta);
        log.setSnapshot(newCount);
        log.setWorkMonth(YearMonth.now().toString());
        log.setLoggedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            log.setNotes(notes.trim());
        }

        repository.save(log);
    }

    public void logFinalSnapshot(UUID orderId, UUID employeeId, int stepOrder, int currentCount) {
        int lastSnapshot = repository
                .findTopByOrderIdAndEmployeeIdAndStepOrderOrderByLoggedAtDesc(orderId, employeeId, stepOrder)
                .map(OrderWorkLog::getSnapshot)
                .orElse(0);

        if (currentCount > lastSnapshot) {
            logProgress(orderId, employeeId, stepOrder, lastSnapshot, currentCount, null);
        } else if (currentCount > 0 && currentCount < lastSnapshot) {
            logProgress(orderId, employeeId, stepOrder, 0, currentCount, null);
        }
    }

    @Transactional(readOnly = true)
    public List<MonthlyWorkSummaryProjection> getMonthlyReport(String month) {
        return repository.monthlyReport(month);
    }

    @Transactional(readOnly = true)
    public Long getEmployeeMonthlyTotal(UUID employeeId, String month) {
        Long total = repository.sumDeltaByEmployeeAndMonth(employeeId, month);
        return total != null ? total : 0L;
    }

    @Transactional(readOnly = true)
    public Long getMyMonthlyTotal(String month) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return getEmployeeMonthlyTotal(currentUserId, month);
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDto> getMyHistory() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return repository.findByEmployeeIdOrderByLoggedAtDesc(currentUserId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDto> getEmployeeHistory(UUID employeeId, String month) {
        return repository.findByEmployeeIdAndWorkMonthOrderByLoggedAtDesc(employeeId, month).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeOrderSummaryDto> getEmployeeMonthlyOrders(UUID employeeId, String month) {
        return repository.findOrderSummaryByEmployeeAndMonth(employeeId, month)
                .stream()
                .map(EmployeeOrderSummaryDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDto> getOrderLogs(UUID orderId) {
        return repository.findByOrderIdOrderByLoggedAtDesc(orderId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private WorkLogResponseDto toDto(OrderWorkLog log) {
        WorkLogResponseDto dto = new WorkLogResponseDto();
        dto.setId(log.getId());
        dto.setOrderId(log.getOrderId());
        dto.setOrderName(log.getOrder() != null ? log.getOrder().getOrderName() : null);
        dto.setEmployeeId(log.getEmployeeId());

        if (log.getEmployee() != null) {
            User user = log.getEmployee();
            String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
            String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
            String fullName = (firstName + " " + lastName).trim();
            dto.setEmployeeFullName(fullName.isEmpty() ? user.getUsername() : fullName);
        }

        dto.setStepOrder(log.getStepOrder());
        dto.setDelta(log.getDelta());
        dto.setSnapshot(log.getSnapshot());
        dto.setWorkMonth(log.getWorkMonth());
        dto.setLoggedAt(log.getLoggedAt());
        dto.setNotes(log.getNotes());
        return dto;
    }
}
