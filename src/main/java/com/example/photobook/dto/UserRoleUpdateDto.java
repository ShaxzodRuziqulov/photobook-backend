package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserRoleUpdateDto {
    private List<UUID> roleIds;
}
