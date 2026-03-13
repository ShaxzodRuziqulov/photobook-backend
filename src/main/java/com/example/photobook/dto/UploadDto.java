package com.example.photobook.dto;

import com.example.photobook.entity.enumirated.OwnerType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UploadDto {
    private OwnerType ownerType;
    private UUID ownerId;
}
