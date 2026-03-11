package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerPagingRequest extends BasePagingRequest {
    private String search;
    private Boolean isActive;
}
