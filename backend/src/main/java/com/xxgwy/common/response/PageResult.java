package com.xxgwy.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private int code;
    private String message;
    private List<T> data;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResult<T> of(List<T> data, long total, int page, int pageSize) {
        return new PageResult<>(200, "ok", data, total, page, pageSize);
    }
}
