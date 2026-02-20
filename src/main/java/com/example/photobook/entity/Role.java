package com.example.photobook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class Role extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;
}