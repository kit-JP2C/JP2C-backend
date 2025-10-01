package com.github.jp2c.auth.service;

import com.github.jp2c.auth.dto.RegisterRequest;
import com.github.jp2c.auth.entity.Account;
import com.github.jp2c.auth.repository.AuthRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthRepository authRepository;

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
    public Account login(String username, String password) {
        Optional<Account> account = authRepository.findByUsername(username);

        if (account.isEmpty()) {
            return null;
        }

        if (passwordEncoder.matches(password, account.get().getPassword())) {
            return account.get();
        }

        return null;
    }
}
