package com.github.jp2c.registry;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketIoMiddleware {
    private final SocketExceptionRegistry exceptionRegistry;
    private final ObjectMapper objectMapper;

    public <T> void registerSafeListener(
        String eventName,
        Class<T> targetType,
        SocketIOServer server,
        SafeEventHandler<T> handler
    ) {
        server.addEventListener(eventName, Object.class, (client, data, ackRequest) -> {
            try {
                // 역직렬화 시도
                T payload = objectMapper.convertValue(data, targetType);
                handler.handle(client, payload, ackRequest);
            } catch (Throwable ex) {
                // Registry로 위임하여 에러 응답
                var errorResponse = exceptionRegistry.resolve(ex);
                if (ackRequest.isAckRequested()) {
                    ackRequest.sendAckData(errorResponse);
                } else {
                    client.sendEvent("error", errorResponse);
                }
            }
        });
    }

    @FunctionalInterface
    public interface SafeEventHandler<T> {
        void handle(SocketIOClient client, T data, AckRequest ackRequest) throws Exception;
    }
}
