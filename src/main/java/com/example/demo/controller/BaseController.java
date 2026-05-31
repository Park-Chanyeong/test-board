package com.example.demo.controller;

import com.example.demo.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

public abstract class BaseController {

    protected <T> ResponseEntity<CommonResponse<T>> execute(HttpStatus status, Supplier<T> action) {
        return ResponseEntity.status(status).body(CommonResponse.of(status, action.get()));
    }

    protected ResponseEntity<Void> executeVoid(Runnable action) {
        action.run();
        return ResponseEntity.noContent().build();
    }
}
