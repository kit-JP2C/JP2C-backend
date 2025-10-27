package com.github.jp2c.exceptionHandler;

import com.github.jp2c.common.dto.CommonErrorResponse;
import com.github.jp2c.exception.BadRequestException;
import com.github.jp2c.exception.ForbiddenException;
import com.github.jp2c.exception.NotFoundException;
import com.github.jp2c.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({BadRequestException.class})
    public CommonErrorResponse handleBadRequestException(BadRequestException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public CommonErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error(ex.getMessage(), ex);
        if (ex.getCause() instanceof ConstraintViolationException) {
            return new CommonErrorResponse(HttpStatus.CONFLICT, "Duplicate Entry");
        }
        return new CommonErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request");
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    public CommonErrorResponse handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request");
    }

    @ExceptionHandler({ForbiddenException.class})
    public CommonErrorResponse handleForbiddenException(ForbiddenException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public CommonErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public CommonErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({NotFoundException.class})
    public CommonErrorResponse handleNotFoundException(NotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({UnauthorizedException.class})
    public CommonErrorResponse handleUnauthorizedException(UnauthorizedException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({RuntimeException.class})
    public CommonErrorResponse handleRuntimeException(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return new CommonErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
