package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateDto {
    private String firstName;
    private String lastName;
    private String profession;
    private String email;
    private String avatarUrl;
    private String phone;
    private String bio;
}
