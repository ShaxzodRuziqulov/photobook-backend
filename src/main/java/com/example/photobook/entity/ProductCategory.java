package com.example.photobook.entity;

import com.example.photobook.entity.enumirated.OrderKind;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "product_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class ProductCategory extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderKind kind;

    @Column(name = "default_pages", length = 80)
    private String defaultPages;
}