package com.example.photobook.entity.enumirated;

import lombok.Getter;

/**
 * Supported lifecycle states for an order.
 */
@Getter
public enum OrderStatus {
    PENDING("KUTILMOQDA"),
    IN_PROGRESS("JARAYONDA"),
    PAUSED("TO'XTATILGAN"),
    COMPLETED("BAJARILGAN"),
    CANCELLED("BEKOR QILINGAN");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
