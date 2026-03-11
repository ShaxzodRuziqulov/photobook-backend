package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialPagingRequest extends BasePagingRequest {
    private String search;
    private String itemType;
}
