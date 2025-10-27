package com.github.jp2c.auth.controller;

import com.github.jp2c.auth.dto.FindUsernameRequest;
import com.github.jp2c.auth.dto.FindUsernameResponse;
import com.github.jp2c.auth.dto.FindUsernameTokenRequest;
import com.github.jp2c.auth.dto.LoginRequest;
import com.github.jp2c.auth.dto.LoginResponse;
import com.github.jp2c.auth.dto.RegisterRequest;
import com.github.jp2c.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증/인가")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입")
    public void register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/find-username/token")
    @Operation(summary = "아이디 찾기 용 토큰 발급")
    public void sendFindUsernameToken(@RequestBody @Valid FindUsernameTokenRequest request) throws MessagingException {
        authService.sendFindUsernameToken(request);
    }

    @PostMapping("/find-username")
    @Operation(summary = "아이디 찾기")
    public FindUsernameResponse findUsername(@RequestBody @Valid FindUsernameRequest request) {
        return authService.findUsername(request);
    }
}
