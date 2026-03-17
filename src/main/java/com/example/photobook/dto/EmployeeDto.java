package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmployeeDto {
    private UUID employeeId;
    private String employeeName;
    private Integer processedCount;
}
