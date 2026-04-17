package com.example.photobook.dto;

import java.math.BigDecimal;
import java.util.UUID;


public record DashboardCategoryCountDto(
        UUID categoryId,
        String categoryName,
        BigDecimal count
) {
}
