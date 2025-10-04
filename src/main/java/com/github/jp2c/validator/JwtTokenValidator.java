package com.github.jp2c.validator;

import com.github.jp2c.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtTokenValidator {
    @Value("${jwt.secret}")
    private String JWT_SECRET;

    private final MacAlgorithm alg = Jwts.SIG.HS256; // ✅ 사용할 알고리즘 지정

    public Claims validateToken(String token) throws UnauthorizedException {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(getSecretSigningKey())
                .build()
                .parseSignedClaims(token);

            return claimsJws.getPayload();
        } catch (Exception ex) {
            throw new UnauthorizedException("토큰 해석 도중 오류 발생. 원인: " + ex.getMessage());
        }
    }

    public SecretKey getSecretSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }
}