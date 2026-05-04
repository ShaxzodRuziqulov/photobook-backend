package com.example.photobook.dto;

import com.example.photobook.projection.EmployeeMonthlyOrderProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EmployeeOrderSummaryDto {
    private UUID orderId;
    private String orderName;
    private String category;
    private String itemType;
    private String status;
    private LocalDate acceptedDate;
    private String kind;
    private LocalDateTime lastLoggedAt;
    private Long totalDelta;

    public static EmployeeOrderSummaryDto from(EmployeeMonthlyOrderProjection p) {
        EmployeeOrderSummaryDto dto = new EmployeeOrderSummaryDto();
        dto.setOrderId(p.getOrderId());
        dto.setOrderName(p.getOrderName());
        dto.setCategory(p.getCategory());
        dto.setItemType(p.getItemType());
        dto.setStatus(p.getStatus());
        dto.setAcceptedDate(p.getAcceptedDate());
        dto.setKind(p.getKind());
        dto.setLastLoggedAt(p.getLastLoggedAt());
        dto.setTotalDelta(p.getTotalDelta());
        return dto;
    }
}
