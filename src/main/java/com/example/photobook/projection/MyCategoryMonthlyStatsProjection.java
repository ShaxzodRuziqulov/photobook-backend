package com.example.photobook.projection;

import java.util.UUID;

public interface MyCategoryMonthlyStatsProjection {
    UUID getCategoryId();
    String getCategoryName();
    String getKind();
    Integer getDefaultPages();
    String getWorkMonth();
    long getOrderCount();
    long getTotalProcessed();
}
