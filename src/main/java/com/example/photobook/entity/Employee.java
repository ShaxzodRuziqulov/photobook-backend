package com.example.photobook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 180)
    private String fullName;

    @Column(length = 120)
    private String profession;

    @Column(name = "phone_number", length = 40)
    private String phoneNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}