package com.github.jp2c.token;

import com.github.jp2c.auth.entity.Account;
import com.github.jp2c.common.constant.TokenType;
import com.github.jp2c.validator.JwtTokenValidator;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 300 * 1000L;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 3600 * 1000 * 24 * 7L;
    private final JwtTokenValidator jwtTokenValidator;

    public String generateToken(Account account, TokenType type) {
        ClaimsBuilder claims = Jwts.claims();
        claims.add("id", account.getId());
        claims.add("nickname", account.getNickname());
        claims.add("username", account.getUsername());

        Date now = new Date();

        Date expiryDate;

        if (type.equals(TokenType.ACCESS)) {
            expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);
        } else {
            throw new IllegalArgumentException("잘못된 토큰 타입: " + type);
        }

        return Jwts.builder()
            .claims(claims.build())
            .subject(account.getUsername())
            .signWith(jwtTokenValidator.getSecretSigningKey())
            .issuedAt(now)
            .expiration(expiryDate)
            .compact();
    }
}