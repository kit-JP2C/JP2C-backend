package com.github.jp2c.common.dto;

import com.github.jp2c.common.constant.ResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class CommonErrorResponse {
    protected ResponseStatus status;
    protected HttpStatus httpStatus;
    protected String message;

    public CommonErrorResponse(HttpStatus httpStatus, String message) {
        this.status = ResponseStatus.ERROR;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public CommonErrorResponse(String message) {
        this.status = ResponseStatus.ERROR;
        this.message = message;
    }
}
