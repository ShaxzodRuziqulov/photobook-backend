package com.example.photobook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadResponseDto {
    private String url;
    private String key;
    private String mime;
    private long size;
}
