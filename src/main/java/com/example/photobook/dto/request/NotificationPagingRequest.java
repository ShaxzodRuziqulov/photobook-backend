package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPagingRequest {
    private String search;
    private String type;
    private Boolean isRead;
    private Boolean actionRequired;
}