package com.example.photobook.entity;

import com.example.photobook.entity.enumirated.OwnerType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Upload extends BaseEntity {
    private String key;
    private String mimeType;
    private Long size;

    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;

    private UUID ownerId;
}
