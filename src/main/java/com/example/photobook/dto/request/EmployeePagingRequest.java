package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeePagingRequest extends BasePagingRequest {
    private String search;
    private String profession;
    private Boolean isActive;
}
