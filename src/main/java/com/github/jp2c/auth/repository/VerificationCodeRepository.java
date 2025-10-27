package com.github.jp2c.auth.repository;

import com.github.jp2c.auth.entity.Account;
import com.github.jp2c.auth.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findOneByAccount(Account account);

    Optional<VerificationCode> findOneByCode(String code);
}
