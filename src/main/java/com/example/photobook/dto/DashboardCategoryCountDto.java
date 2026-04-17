package com.example.photobook.dto;

import java.util.UUID;

/**
 * Lightweight dashboard response for product-category order counts.
 */
public record DashboardCategoryCountDto(
        UUID categoryId,
        String categoryName,
        long count
) {
}
