package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MaterialAdjustDto {
    private BigDecimal delta;
    private String reason;
}
