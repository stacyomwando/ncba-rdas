package com.ncba.rdas.dto;

import java.util.List;

public class PagedResponse<T> {
    private String status;
    private List<T> data;
    private Meta pagination;

    public static <T> PagedResponse<T> of(List<T> data, int page, int size, long total) {
        PagedResponse<T> r = new PagedResponse<>();
        r.status = "success";
        r.data = data;
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        r.pagination = new Meta(page, size, total, totalPages);
        return r;
    }

    public String getStatus() { return status; }
    public List<T> getData() { return data; }
    public Meta getPagination() { return pagination; }

    public static class Meta {
        private int page, size, totalPages;
        private long totalElements;
        public Meta(int page, int size, long totalElements, int totalPages) {
            this.page = page; this.size = size;
            this.totalElements = totalElements; this.totalPages = totalPages;
        }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
    }
}
