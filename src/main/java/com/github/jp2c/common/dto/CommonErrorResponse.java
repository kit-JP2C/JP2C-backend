package com.github.jp2c.common.dto;

import com.github.jp2c.common.constant.ResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonErrorResponse {
    protected ResponseStatus status;
    protected String message;

    public CommonErrorResponse(String message) {
        this.status = ResponseStatus.ERROR;
        this.message = message;
    }
}
