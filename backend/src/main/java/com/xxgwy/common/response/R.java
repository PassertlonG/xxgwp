package com.xxgwy.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class R<T> {
    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok(T data) {
        return new R<>(200, "ok", data);
    }

    public static <T> R<T> ok() {
        return new R<>(200, "ok", null);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    public static <T> R<T> failed(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> failed(String message) {
        return new R<>(500, message, null);
    }
}
