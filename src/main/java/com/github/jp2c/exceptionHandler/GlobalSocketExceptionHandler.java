package com.github.jp2c.exceptionHandler;

import com.github.jp2c.annotation.SocketExceptionHandler;
import com.github.jp2c.common.dto.CommonErrorResponse;
import com.github.jp2c.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GlobalSocketExceptionHandler {
    @SocketExceptionHandler(BadRequestException.class)
    public CommonErrorResponse handleBadRequestException(BadRequestException ex) {
        log.error("[BadRequestException]: {}", ex.getMessage(), ex);
        return new CommonErrorResponse(ex.getMessage());
    }

    @SocketExceptionHandler(RuntimeException.class)
    public CommonErrorResponse handleRuntimeException(RuntimeException ex) {
        log.error("[RuntimeException]: {}", ex.getMessage(), ex);
        return new CommonErrorResponse(ex.getMessage());
    }
}
