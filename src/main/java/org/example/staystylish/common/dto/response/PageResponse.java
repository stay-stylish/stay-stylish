package org.example.staystylish.common.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int size;
    private final int number;

    public PageResponse(List<T> content, long totalElements, int totalPages, int size, int number) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.size = size;
        this.number = number;
    }

    public static <T> PageResponse<T> fromPage(Page<T> pagedData) {
        return new PageResponse<>(
                pagedData.getContent(),
                pagedData.getTotalElements(),
                pagedData.getTotalPages(),
                pagedData.getPageable().getPageSize(),
                pagedData.getPageable().getPageNumber()
        );
    }
}

