package com.example.demo.controller;

import com.example.demo.dto.CommonResponse;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.RefreshDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.dto.TokenDto;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "회원가입", description = "username과 password로 신규 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
    })
    @PostMapping("/register")
    public ResponseEntity<CommonResponse<String>> register(@Valid @RequestBody RegisterDto dto) {
        return execute(HttpStatus.CREATED, () -> { authService.register(dto); return "회원가입 성공"; });
    }

    @Operation(summary = "로그인", description = "인증 성공 시 Access Token과 Refresh Token을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 발급"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenDto>> login(@Valid @RequestBody LoginDto dto) {
        return execute(HttpStatus.OK, () -> authService.login(dto));
    }

    @Operation(
            summary = "토큰 갱신",
            description = "Refresh Token으로 새 Access Token을 발급합니다. Refresh Token은 만료 전까지 재사용 가능(7일)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenDto>> refresh(@RequestBody RefreshDto dto) {
        return execute(HttpStatus.OK, () -> authService.refresh(dto.getRefreshToken()));
    }
}
