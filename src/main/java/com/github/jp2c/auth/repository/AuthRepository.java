package com.github.jp2c.auth.repository;

import com.github.jp2c.auth.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
}
