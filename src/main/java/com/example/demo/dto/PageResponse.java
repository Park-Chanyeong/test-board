package com.example.demo.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/** Page<T> 의 불필요한 내부 필드를 제거하고 필요한 정보만 담는 응답 DTO */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    private PageResponse(Page<T> source) {
        this.content = source.getContent();
        this.page = source.getNumber();
        this.size = source.getSize();
        this.totalElements = source.getTotalElements();
        this.totalPages = source.getTotalPages();
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page);
    }
}