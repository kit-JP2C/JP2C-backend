package com.github.jp2c.registrar;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.github.jp2c.annotation.ConnectEvent;
import com.github.jp2c.annotation.DisconnectEvent;
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
public class CommonEventRegistrar implements ApplicationListener<ContextRefreshedEvent> {
    private final SocketIOServer server;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();

        Map<String, Object> beans = context.getBeansWithAnnotation(Component.class);
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(ConnectEvent.class)) {
                    Class<?>[] paramTypes = method.getParameterTypes();

                    log.info("연결 이벤트 등록됨: {}.{}", bean.getClass().getSimpleName(), method.getName());

                    server.addConnectListener(client -> {
                        try {
                            Object[] args = Arrays.stream(paramTypes)
                                .map(type -> type.isAssignableFrom(SocketIOClient.class) ? client : null)
                                .toArray();

                            method.setAccessible(true);
                            method.invoke(bean, args);
                        } catch (Exception e) {
                            log.error("ConnectEvent 처리 중 예외 - {}: {}", method.getName(), e.getMessage(), e);
                        }
                    });
                }

                if (method.isAnnotationPresent(DisconnectEvent.class)) {
                    Class<?>[] paramTypes = method.getParameterTypes();

                    log.info("연결 해제 이벤트 등록됨: {}.{}", bean.getClass().getSimpleName(), method.getName());

                    server.addDisconnectListener(client -> {
                        try {
                            Object[] args = Arrays.stream(paramTypes)
                                .map(type -> type.isAssignableFrom(SocketIOClient.class) ? client : null)
                                .toArray();

                            method.setAccessible(true);
                            method.invoke(bean, args);
                        } catch (Exception e) {
                            log.error("DisconnectEvent 처리 중 예외 - {}: {}", method.getName(), e.getMessage(), e);
                        }
                    });
                }
            }
        }
    }
}
