package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CustomerDto {
    private UUID id;
    private String fullName;
    private String phone;
    private String notes;
}