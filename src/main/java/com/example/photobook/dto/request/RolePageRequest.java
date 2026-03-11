package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePageRequest extends BasePagingRequest {
    private String search;
}
