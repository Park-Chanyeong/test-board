package com.example.demo.exception;

import com.example.demo.dto.CommonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

/**
 * 컨트롤러에서 발생한 모든 예외를 가로채 CommonResponse 형태로 응답하는 전역 예외 처리기.
 * 예외 종류별로 적절한 HTTP 상태코드와 메시지를 내려준다.
 */
@RestControllerAdvice(basePackages = "com.example.demo.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** {@code @Valid} 검증 실패 → 400, "필드명: 메시지" 형태로 합쳐서 반환 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /** 서비스에서 던진 비즈니스 예외 → 예외가 담고 있는 status 코드로 응답 */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<CommonResponse<Void>> handleApp(AppException ex) {
        return errorResponse(ex.getStatus(), ex.getMessage());
    }

    /** 로그인 실패 (아이디/비밀번호 불일치) → 401 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** 권한 없음 (남의 글 수정·삭제 시도) → 403 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return errorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /** Spring 내부에서 던지는 ResponseStatusException → 해당 status 코드로 응답 */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CommonResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return errorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), message);
    }

    /** 예상치 못한 예외 → 500, 상세 내용은 서버 로그에만 기록 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    private ResponseEntity<CommonResponse<Void>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(CommonResponse.error(status, message));
    }
}
