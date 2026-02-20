package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ExpenseDto {
    private UUID id;
    private UUID categoryId;
    private UUID materialId;
    private String name;
    private BigDecimal price;
    private String description;
    private String paymentMethod;
    private String receiptImageUrl;
    private LocalDate expenseDate;
}
