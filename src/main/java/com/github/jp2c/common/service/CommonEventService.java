package com.github.jp2c.common.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.jp2c.common.clientKeys.ClientKeys;
import com.github.jp2c.status.service.SocketConnectionTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonEventService {
    private final SocketConnectionTracker socketConnectionTracker;

    public void handleConnect(SocketIOClient client) {
        String nickname = ClientKeys.NICKNAME.get(client);
        socketConnectionTracker.onConnect(client.getSessionId().toString());
        log.info("유저 접속 : {}", nickname);
    }

    public void handleDisconnect(SocketIOClient client) {
        String nickname = ClientKeys.NICKNAME.get(client);
        socketConnectionTracker.onDisconnect(client.getSessionId().toString());
        log.info("유저 접속 해제 : {}", nickname);
    }
}
