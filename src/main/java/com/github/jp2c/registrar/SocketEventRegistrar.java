package com.github.jp2c.registrar;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.annotation.SocketEvent;
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
@RequiredArgsConstructor
@Slf4j
public class SocketEventRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    private final SocketIOServer server;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();

        Map<String, Object> beans = context.getBeansWithAnnotation(Component.class);
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                SocketEvent annotation = method.getAnnotation(SocketEvent.class);
                if (annotation == null) continue;

                String eventName = annotation.value();
                Class<?>[] paramTypes = method.getParameterTypes();

                Class<?> dataType = Void.class;
                int nonFrameworkParamCount = 0;
                for (Class<?> type : paramTypes) {
                    if (!SocketIOClient.class.isAssignableFrom(type)
                        && !AckRequest.class.isAssignableFrom(type)) {
                        nonFrameworkParamCount++;
                        if (nonFrameworkParamCount == 1) {
                            dataType = type;
                        }
                    }
                }
                if (nonFrameworkParamCount > 1) {
                    log.error("이벤트 리스너 '{}' → {}.{}: 여러 개의 데이터 파라미터가 있습니다. 하나만 허용됩니다. 등록을 건너뜁니다.",
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

                    } catch (Exception e) {
                        log.error("소켓 이벤트 처리 중 예외 발생 - {}: {}", eventName, e.getMessage(), e);
                    }
                });
            }
        }
    }
}