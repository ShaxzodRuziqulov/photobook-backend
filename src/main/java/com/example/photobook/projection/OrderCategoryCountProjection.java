package com.example.photobook.projection;

import java.util.UUID;

/**
 * Projection used by grouped product-category aggregation queries.
 */
public interface OrderCategoryCountProjection {
    UUID getCategoryId();

    String getCategoryName();

    long getCount();
}
