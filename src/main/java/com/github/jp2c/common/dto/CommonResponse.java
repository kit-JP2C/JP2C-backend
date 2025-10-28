package com.github.jp2c.common.dto;

import com.github.jp2c.common.constant.ResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class CommonResponse<T> extends CommonErrorResponse {
    private T data;

    public CommonResponse(T data) {
        this.httpStatus = HttpStatus.OK;
        this.status = ResponseStatus.OK;
        this.data = data;
    }

    public CommonResponse(HttpStatus status, T data) {
        this(data);

        this.httpStatus = status;
    }
}
