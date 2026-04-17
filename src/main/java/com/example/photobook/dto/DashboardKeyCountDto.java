package com.example.photobook.dto;

/**
 * Lightweight dashboard response for enum-based statistics.
 */
public record DashboardKeyCountDto(
        String key,
        long count
) {
}
