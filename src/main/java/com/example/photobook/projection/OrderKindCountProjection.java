package com.example.photobook.projection;

import com.example.photobook.entity.enumirated.OrderKind;

/**
 * Projection used by grouped order-kind aggregation queries.
 */
public interface OrderKindCountProjection {
    OrderKind getKind();

    long getCount();
}
