package com.example.photobook.dto.request;

import com.example.photobook.entity.enumirated.OrderKind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCategoryPagingRequest {
    private OrderKind kind;
    private String search;
}
