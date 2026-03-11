package com.example.photobook.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int limit;
    private long total;
    private int totalPages;

    public PageResponse(Page<T> page) {
        this.items = page.getContent();
        this.page = page.getNumber() + 1;
        this.limit = page.getSize();
        this.total = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}
