package com.github.jp2c.auth.service;

import com.github.jp2c.auth.dto.FindUsernameRequest;
import com.github.jp2c.auth.dto.FindUsernameResponse;
import com.github.jp2c.auth.dto.FindUsernameTokenRequest;
import com.github.jp2c.auth.dto.LoginRequest;
import com.github.jp2c.auth.dto.LoginResponse;
import com.github.jp2c.auth.dto.RegisterRequest;
import com.github.jp2c.auth.entity.Account;
import com.github.jp2c.auth.entity.VerificationCode;
import com.github.jp2c.auth.repository.AuthRepository;
import com.github.jp2c.auth.repository.VerificationCodeRepository;
import com.github.jp2c.common.constant.TokenType;
import com.github.jp2c.exception.BadRequestException;
import com.github.jp2c.exception.UnauthorizedException;
import com.github.jp2c.token.TokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthRepository authRepository;
    private final TokenService tokenService;
    private final JavaMailSender javaMailSender;
    private final VerificationCodeRepository verificationCodeRepository;
    private final long VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    @Value("${spring.mail.from}")
    private String from;

    @Transactional
    public void register(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Account account = new Account(
            request.getUsername(),
            encodedPassword,
            request.getEmail(),
            request.getNickname()
        );

        authRepository.save(account);
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public void sendFindUsernameToken(FindUsernameTokenRequest request) throws MessagingException {
        Account account = authRepository.findByNicknameAndIsAppliedIsTrue(request.getNickname())
            .orElseThrow(() -> new UnauthorizedException("계정이 존재하지 않거나 허가되지 않은 사용자입니다."));

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES);

        VerificationCode verificationCode = verificationCodeRepository.findOneByAccount(account)
            .orElse(new VerificationCode(code, expireAt, account));

        if (verificationCode.getId() != null) {
            verificationCode.update(code, expireAt);
        }

        verificationCodeRepository.save(verificationCode);

        helper.setFrom(from);
        helper.setTo(account.getEmail());
        helper.setSubject("[JP2C] 아이디 찾기 - 인증 토큰");

        String body = String.format("%s님의 인증 토큰은 %s입니다. 토큰은 10분 간 유효합니다.", request.getNickname(), code);
        helper.setText(body);

        javaMailSender.send(message);
    }

    @Transactional(readOnly = true)
    public FindUsernameResponse findUsername(FindUsernameRequest request) {
        VerificationCode verificationCode = verificationCodeRepository.findOneByCode(request.getCode())
            .orElse(null);

        if (verificationCode == null || verificationCode.isExpired()) {
            if (verificationCode != null && verificationCode.isExpired()) {
                verificationCodeRepository.delete(verificationCode);
            }

            throw new BadRequestException("인증 토큰이 없거나 만료되었습니다.");
        }

        verificationCodeRepository.delete(verificationCode);

        return new FindUsernameResponse(verificationCode.getAccount().getUsername());
    }
}
