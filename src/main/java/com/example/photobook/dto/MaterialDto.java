package com.example.photobook.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class MaterialDto {
    private UUID id;
    private String itemName;
    private String itemType;
    private String unitName;
    private BigDecimal quantity = BigDecimal.ZERO;
}
