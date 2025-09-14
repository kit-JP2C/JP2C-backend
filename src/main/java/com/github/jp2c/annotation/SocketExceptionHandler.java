package com.github.jp2c.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketExceptionHandler {
    Class<? extends Throwable> value(); // 처리할 예외 클래스
}