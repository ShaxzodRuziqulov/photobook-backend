package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialPagingRequest {
    private String search;
    private String itemType;
}
