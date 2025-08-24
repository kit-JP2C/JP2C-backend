package com.github.jp2c.common.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.annotation.ConnectEvent;
import com.github.jp2c.annotation.DisconnectEvent;
import com.github.jp2c.common.service.CommonEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonEventController {
    private final CommonEventService commonEventService;

    @ConnectEvent
    public void handleConnect(SocketIOClient client) {
        commonEventService.handleConnect(client);
    }

    @DisconnectEvent
    public void handleDisconnect(SocketIOClient client) {
        commonEventService.handleDisconnect(client);
    }
}
