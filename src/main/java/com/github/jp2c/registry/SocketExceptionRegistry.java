package com.github.jp2c.registry;

import com.github.jp2c.annotation.SocketExceptionHandler;
import com.github.jp2c.common.dto.CommonErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SocketExceptionRegistry {
    private final Map<Class<? extends Throwable>, MethodHandler> handlers = new HashMap<>();

    public void register(Object bean) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            SocketExceptionHandler ann = method.getAnnotation(SocketExceptionHandler.class);
            if (ann == null) continue;

            Class<? extends Throwable> exType = ann.value();
            method.setAccessible(true);
            handlers.put(exType, new MethodHandler(bean, method));
            log.info("예외 처리기 등록: {}", exType.getSimpleName());
        }
    }

    public CommonErrorResponse resolve(Throwable ex) {
        for (Map.Entry<Class<? extends Throwable>, MethodHandler> entry : handlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(ex.getClass())) {
                try {
                    return (CommonErrorResponse) entry.getValue().invoke(ex);
                } catch (Exception e) {
                    return new CommonErrorResponse(e.getMessage());
                }
            }
        }
        return new CommonErrorResponse(ex.getMessage());
    }

    private record MethodHandler(Object bean, Method method) {
        public Object invoke(Throwable ex) throws Exception {
            return method.invoke(bean, ex);
        }
    }
}