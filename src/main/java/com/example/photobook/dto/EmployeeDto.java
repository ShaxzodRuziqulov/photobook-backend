package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmployeeDto {
    private UUID id;
    private String fullName;
    private String profession;
    private String phoneNumber;
    private Boolean isActive = true;
}
