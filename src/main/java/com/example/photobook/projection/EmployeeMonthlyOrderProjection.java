package com.example.photobook.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EmployeeMonthlyOrderProjection {
    UUID getOrderId();
    String getOrderName();
    String getCategory();
    String getItemType();
    String getStatus();
    LocalDate getAcceptedDate();
    String getKind();
    LocalDateTime getLastLoggedAt();
    Long getTotalDelta();
}