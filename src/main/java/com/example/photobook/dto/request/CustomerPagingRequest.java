package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerPagingRequest {
    private String search;
    private Boolean isActive;
}
