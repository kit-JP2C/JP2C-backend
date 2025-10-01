package com.github.jp2c.common.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonEventService {
    public void handleConnect(SocketIOClient client) {
        String nickname = client.get("nickname");
        log.info("유저 접속 : {}", nickname);
    }

    public void handleDisconnect(SocketIOClient client) {
        String nickname = client.get("nickname");
        log.info("유저 접속 해제 : {}", nickname);
    }
}
