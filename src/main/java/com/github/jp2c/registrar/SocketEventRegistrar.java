package com.github.jp2c.registrar;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.annotation.SocketEvent;
import com.github.jp2c.common.dto.CommonErrorResponse;
import com.github.jp2c.registry.SocketExceptionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketEventRegistrar implements ApplicationListener<ContextRefreshedEvent> {
    private final SocketIOServer server;
    private final ApplicationContext context;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        SocketExceptionRegistry exceptionRegistry = context.getBean(SocketExceptionRegistry.class);

        Map<String, Object> beans = context.getBeansWithAnnotation(Component.class);
        for (Object bean : beans.values()) {
            // 예외 핸들러 등록도 이 시점에 처리
            exceptionRegistry.register(bean);

            for (Method method : bean.getClass().getDeclaredMethods()) {
                SocketEvent annotation = method.getAnnotation(SocketEvent.class);
                if (annotation == null) continue;

                String eventName = annotation.value();
                Class<?>[] paramTypes = method.getParameterTypes();

                // 단일 데이터 타입 추출
                Class<?> dataType = Void.class;
                int nonFrameworkParamCount = 0;
                for (Class<?> type : paramTypes) {
                    if (!SocketIOClient.class.isAssignableFrom(type)
                        && !AckRequest.class.isAssignableFrom(type)) {
                        nonFrameworkParamCount++;
                        if (nonFrameworkParamCount == 1) dataType = type;
                    }
                }

                if (nonFrameworkParamCount > 1) {
                    log.error("이벤트 리스너 '{}' → {}.{}: 데이터 파라미터가 2개 이상입니다. 등록 건너뜀.",
                        eventName, bean.getClass().getSimpleName(), method.getName());
                    continue;
                }

                log.info("이벤트 리스너 등록됨: '{}' → {}.{}",
                    eventName, bean.getClass().getSimpleName(), method.getName());

                final Class<?> finalDataType = dataType;

                server.addEventListener(eventName, finalDataType, (client, data, ackSender) -> {
                    try {
                        Object[] args = Arrays.stream(paramTypes)
                            .map(type -> {
                                if (SocketIOClient.class.isAssignableFrom(type)) return client;
                                if (AckRequest.class.isAssignableFrom(type)) return ackSender;
                                if (finalDataType.isAssignableFrom(type) || type.equals(finalDataType)) return data;
                                return null;
                            })
                            .toArray();

                        method.invoke(bean, args);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause();
                        CommonErrorResponse errorResponse = exceptionRegistry.resolve(cause);
                        if (ackSender.isAckRequested()) {
                            ackSender.sendAckData(errorResponse);
                        }
                    }
                });
            }
        }
    }
}