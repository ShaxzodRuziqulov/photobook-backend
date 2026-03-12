package com.example.photobook.entity;


import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_category_id", columnList = "category_id"),
                @Index(name = "idx_orders_customer_id", columnList = "customer_id")
        }
)
public class Order extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderKind kind;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(name = "order_name", nullable = false, length = 200)
    private String orderName;

    @Column(name = "item_type", length = 120)
    private String itemType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "receiver_name", nullable = false, length = 180)
    private String receiverName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "order_employees",
            joinColumns = @JoinColumn(name = "order_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false),
            indexes = {
                    @Index(name = "idx_order_employees_order_id", columnList = "order_id"),
                    @Index(name = "idx_order_employees_user_id", columnList = "user_id")
            }
    )
    private Set<User> employees = new HashSet<>();

    @Column(name = "page_count", nullable = false)
    private Integer pageCount = 0;

    @Column(nullable = false)
    private Integer amount = 0;

    @Column(name = "processed_count", nullable = false)
    private Integer processedCount = 0;

    @Column(name = "accepted_date", nullable = false)
    private LocalDate acceptedDate;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}
