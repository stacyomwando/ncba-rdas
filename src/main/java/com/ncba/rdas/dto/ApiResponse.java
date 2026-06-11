package com.ncba.rdas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.status = "success"; r.data = data; return r;
    }
    public static <T> ApiResponse<T> error(String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.status = "error"; r.message = msg; return r;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
