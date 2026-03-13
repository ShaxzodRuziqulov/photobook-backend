package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UploadResponseDto {
    private UUID id;
    private String url;
    private String key;
    private String mime;
    private long size;
}
