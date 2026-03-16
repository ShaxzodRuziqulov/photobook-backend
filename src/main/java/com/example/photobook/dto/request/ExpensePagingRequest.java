package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ExpensePagingRequest {
    private String search;
    private UUID categoryId;
    private UUID materialId;
    private String paymentMethod;
}
