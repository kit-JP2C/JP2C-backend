package com.github.jp2c.common.dto;

import com.github.jp2c.common.constant.ResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonResponse<T> extends CommonErrorResponse {
    private T data;

    public CommonResponse(T data) {
        this.status = ResponseStatus.OK;
        this.data = data;
    }
}
