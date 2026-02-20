package com.example.photobook.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "materials")
public class Material extends BaseEntity {

    @Column(name = "item_name", nullable = false, length = 180)
    private String itemName;

    @Column(name = "item_type", length = 120)
    private String itemType;

    @Column(name = "unit_name", length = 60)
    private String unitName;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;
}
