package com.example.photobook.entity.enumirated;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("KUTILMOQDA"),
    IN_PROGRESS("JARAYONDA"),
    COMPLETED("BAJARILGAN");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}