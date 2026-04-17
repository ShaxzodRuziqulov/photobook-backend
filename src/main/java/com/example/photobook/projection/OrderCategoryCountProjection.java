package com.example.photobook.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderCategoryCountProjection {
    UUID getCategoryId();

    String getCategoryName();

    BigDecimal getTotalAmount();
}
