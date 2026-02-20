package com.example.photobook.dto;

import com.example.photobook.entity.enumirated.OrderKind;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductCategoryDto {
    private UUID id;
    private String name;
    private OrderKind kind;
    private String defaultPages;
}