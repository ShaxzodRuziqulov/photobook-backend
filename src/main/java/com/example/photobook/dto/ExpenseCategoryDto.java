package com.example.photobook.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ExpenseCategoryDto {
    private UUID id;
    private String name;
}
