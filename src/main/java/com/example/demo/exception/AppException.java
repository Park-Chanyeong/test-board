package com.example.demo.exception;

import org.springframework.http.HttpStatus;

/**
 * 서비스 레이어에서 의도적으로 던지는 비즈니스 예외.
 * HTTP 상태코드를 직접 담아 GlobalExceptionHandler가 그대로 응답에 사용한다.
 *
 * <p>사용 예: {@code throw new AppException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id);}
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
