package com.github.jp2c.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindUsernameTokenRequest {
    @NotBlank
    private String nickname;
}
