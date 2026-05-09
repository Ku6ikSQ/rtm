package com.ttkhnvv.rtm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T, S> PageResponse<T> of(Page<S> sourcePage, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(sourcePage.getNumber())
                .size(sourcePage.getSize())
                .totalElements(sourcePage.getTotalElements())
                .totalPages(sourcePage.getTotalPages())
                .build();
    }
}
