package net.clanhalls.plugin.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class APIResponse<T> {
    private final APIError error;
    private final T data;

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(null, data);
    }

    public static <T> APIResponse<T> failure(int status, String message) {
        return new APIResponse<>(new APIError(status, message), null);
    }

    @Getter
    @RequiredArgsConstructor
    public static class APIError {
        private final int status;
        private final String message;
    }
}