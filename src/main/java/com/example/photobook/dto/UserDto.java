package com.example.photobook.dto;

import com.example.photobook.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String phone;
    private String bio;
    private Boolean isActive;
    private Set<Role> roles;
}
