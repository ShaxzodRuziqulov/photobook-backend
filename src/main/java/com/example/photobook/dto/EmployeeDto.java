package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmployeeDto {
    private UUID employeeIds;
    private String employeeNames;
}
