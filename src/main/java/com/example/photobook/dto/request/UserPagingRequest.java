package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPagingRequest extends BasePagingRequest {
    private String search;
    private Boolean isActive;
    private String role;
}
