package com.github.jp2c.auth.service;

import com.github.jp2c.auth.dto.LoginRequest;
import com.github.jp2c.auth.dto.LoginResponse;
import com.github.jp2c.auth.dto.RegisterRequest;
import com.github.jp2c.auth.entity.Account;
import com.github.jp2c.auth.repository.AuthRepository;
import com.github.jp2c.common.constant.TokenType;
import com.github.jp2c.exception.UnauthorizedException;
import com.github.jp2c.token.TokenService;
import com.github.jp2c.validator.JwtTokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthRepository authRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final TokenService tokenService;

    @Transactional
    public void register(@Valid RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Account account = new Account(
            request.getUsername(),
            encodedPassword,
            request.getNickname()
        );

        authRepository.save(account);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String errorMessage = "계정이 존재하지 않거나 허가되지 않은 사용자입니다.";
        Account account = authRepository.findByUsernameAndIsAppliedIsTrue(request.getUsername())
            .orElseThrow(() -> new UnauthorizedException(errorMessage));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new UnauthorizedException(errorMessage);
        }

        String accessToken = tokenService.generateToken(account, TokenType.ACCESS);

        return new LoginResponse(account, accessToken);
    }
}
