package com.example.demo.controller;

import com.example.demo.dto.CommonResponse;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.RefreshDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.dto.TokenDto;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ResponseEntity<CommonResponse<String>> register(@Valid @RequestBody RegisterDto dto) {
        return execute(HttpStatus.CREATED, () -> { authService.register(dto); return "회원가입 성공"; });
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenDto>> login(@Valid @RequestBody LoginDto dto) {
        return execute(HttpStatus.OK, () -> authService.login(dto));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenDto>> refresh(@Valid @RequestBody RefreshDto dto) {
        return execute(HttpStatus.OK, () -> authService.refresh(dto.getRefreshToken()));
    }
}
