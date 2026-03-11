package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseCategoryPagingRequest extends BasePagingRequest {
    private String search;
}
