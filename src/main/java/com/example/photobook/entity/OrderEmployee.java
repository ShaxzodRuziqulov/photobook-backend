package com.example.photobook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "order_employees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_employees_order_user", columnNames = {"order_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_order_employees_order_id", columnList = "order_id"),
                @Index(name = "idx_order_employees_user_id", columnList = "user_id")
        }
)
public class OrderEmployee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "processed_count", nullable = false)
    private Integer processedCount = 0;
}
