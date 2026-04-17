package com.example.photobook.projection;

import com.example.photobook.entity.enumirated.OrderStatus;

/**
 * Projection used by grouped order-status aggregation queries.
 */
public interface OrderStatusCountProjection {
    OrderStatus getStatus();

    long getCount();
}
