package com.example.photobook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequestDto {
    @JsonProperty("refresh_token")
    private String refreshToken;
}
