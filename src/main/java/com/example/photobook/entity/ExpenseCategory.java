package com.example.photobook.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "expense_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class ExpenseCategory extends BaseEntity {

    @Column(nullable = false, length = 140)
    private String name;
}
