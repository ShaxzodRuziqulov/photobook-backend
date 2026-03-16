package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPagingRequest {
    private String search;
    private Boolean isActive;
    private String role;
}
