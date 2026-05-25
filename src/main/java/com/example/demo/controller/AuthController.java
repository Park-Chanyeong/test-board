package com.example.demo.controller;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.dto.TokenDto;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public TokenDto login(@RequestBody LoginDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/refresh")
    public TokenDto refresh(@RequestBody Map<String, String> body) {
        return authService.refresh(body.get("refreshToken"));
    }
}