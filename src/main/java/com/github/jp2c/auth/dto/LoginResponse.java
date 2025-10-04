package com.github.jp2c.auth.dto;

import com.github.jp2c.auth.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String nickname;
    private String accessToken;

    public LoginResponse(Account account, String accessToken) {
        this.nickname = account.getNickname();
        this.accessToken = accessToken;
    }
}
