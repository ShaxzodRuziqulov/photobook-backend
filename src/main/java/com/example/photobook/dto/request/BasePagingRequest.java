package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasePagingRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "updatedAt";
    private String direction = "DESC";
}
