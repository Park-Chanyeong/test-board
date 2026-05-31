package com.example.demo.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonResponse<T> {

    private final int status;
    private final String message;
    private final T data;

    public static <T> CommonResponse<T> of(HttpStatus status, T data) {
        return new CommonResponse<>(status.value(), status.getReasonPhrase(), data);
    }

    public static CommonResponse<Void> error(HttpStatus status, String message) {
        return new CommonResponse<>(status.value(), message, null);
    }
}
