package com.example.demo.controller;

import com.example.demo.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

/**
 * 모든 REST 컨트롤러의 공통 응답 처리를 담당하는 추상 클래스.
 *
 * <p>컨트롤러에서 반복되는 ResponseEntity + CommonResponse 조합 코드를
 * execute / executeVoid 두 메서드로 통일한다.
 *
 * <p>사용 예:
 * <pre>
 *   // 반환값 있는 경우 (GET, POST, PUT)
 *   return execute(HttpStatus.OK, () -> postService.findById(id));
 *
 *   // 반환값 없는 경우 (DELETE)
 *   return executeVoid(() -> postService.delete(id, username));
 * </pre>
 */
public abstract class BaseController {

    /**
     * 서비스 로직을 실행하고 결과를 CommonResponse 바디에 담아 반환한다.
     *
     * @param status HTTP 상태 코드 (예: HttpStatus.OK, HttpStatus.CREATED)
     * @param action 실행할 서비스 로직. 반환값이 응답의 data 필드가 된다.
     */
    protected <T> ResponseEntity<CommonResponse<T>> execute(HttpStatus status, Supplier<T> action) {
        return ResponseEntity.status(status).body(CommonResponse.of(status, action.get()));
    }

    /**
     * 반환값 없는 서비스 로직을 실행하고 200 OK + data=null 로 응답한다.
     *
     * @param action 실행할 서비스 로직 (주로 삭제 작업)
     */
    protected ResponseEntity<CommonResponse<Void>> executeVoid(Runnable action) {
        action.run();
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, null));
    }
}
