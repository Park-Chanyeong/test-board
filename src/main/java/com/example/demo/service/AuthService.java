package com.example.demo.service;

import com.example.demo.dto.LoginDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.dto.TokenDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public void register(RegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new AppException(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다: " + dto.getUsername());
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public TokenDto login(LoginDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        refreshTokenRepository.save(refreshToken);

        return new TokenDto(accessToken, refreshTokenValue);
    }

    @Transactional
    public TokenDto refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 Refresh Token입니다."));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh Token이 만료되었습니다.");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUser().getUsername());
        return new TokenDto(newAccessToken, refreshTokenValue);
    }
}
