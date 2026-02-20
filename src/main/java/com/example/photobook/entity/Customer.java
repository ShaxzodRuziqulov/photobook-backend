package com.example.photobook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 180)
    private String fullName;

    @Column(length = 40)
    private String phone;

    private String notes;

    @Column(nullable = false)
    private Boolean isActive = true;
}