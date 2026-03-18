package com.example.photobook.dto;

import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskUpdateDto {
    private Integer processedCount;
    private String notes;
    private EmployeeWorkStatus workStatus;
}
