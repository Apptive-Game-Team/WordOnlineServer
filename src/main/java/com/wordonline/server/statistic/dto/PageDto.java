package com.wordonline.server.statistic.dto;

import java.util.List;

/**
 * A page of results plus the totals the caller needs to render pagination controls.
 *
 * @param content    the rows on this page
 * @param page       zero-based page index
 * @param size       requested page size
 * @param totalCount total rows matching the filter across all pages
 */
public record PageDto<T>(
        List<T> content,
        int page,
        int size,
        long totalCount
) {
    public int totalPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) ((totalCount + size - 1) / size);
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return page + 1 < totalPages();
    }
}
