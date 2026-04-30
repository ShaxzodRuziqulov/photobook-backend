package com.example.photobook.projection;

import java.util.UUID;

public interface MonthlyWorkSummaryProjection {
    UUID getEmployeeId();
    String getEmployeeFullName();
    Long getTotalDelta();
}
