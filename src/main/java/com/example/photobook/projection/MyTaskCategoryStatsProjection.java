package com.example.photobook.projection;

import java.util.UUID;

public interface MyTaskCategoryStatsProjection {
    UUID getCategoryId();
    String getCategoryName();
    long getOrderCount();
    long getTotalProcessed();
}
