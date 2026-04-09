package com.example.photobook.dto;

import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmployeeDto {
    private UUID employeeId;
    private String employeeName;
    private Integer processedCount;
    private Integer stepOrder;
    private String notes;
    private EmployeeWorkStatus workStatus;
}
