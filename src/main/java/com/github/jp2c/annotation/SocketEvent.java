package com.github.jp2c.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketEvent {
    String value();
    /**
     * Swagger 문서에 표시될 간단한 요약
     */
    String summary() default "";

    /**
     * Swagger 문서에 표시될 상세 설명
     */
    String description() default "";

    /**
     * 요청 payload 클래스
     * 기본값은 Void.class → payload 없음
     */
    Class<?> payload() default Void.class;

    /**
     * 응답 payload 클래스
     * 기본값은 Void.class → 응답 없음
     */
    Class<?> response() default Void.class;
}
