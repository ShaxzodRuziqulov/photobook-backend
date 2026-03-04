package com.example.photobook.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AuthUserResponse {
    private UUID id;
    private String name;
    private String email;
    private List<String> roles;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String phone;
    private String bio;
}
