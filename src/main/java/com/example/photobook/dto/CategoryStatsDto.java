package com.example.photobook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategoryStatsDto {
    private final UUID categoryId;
    private final String categoryName;
    private final String kind;
    private final Integer defaultPages;
    private final String workMonth;
    private final long orderCount;
    private final long totalProcessed;
}
