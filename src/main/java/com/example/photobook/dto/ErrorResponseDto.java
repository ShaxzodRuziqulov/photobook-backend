package com.example.photobook.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ErrorResponseDto {
    private String message;
    private Map<String, List<String>> errors;
}
