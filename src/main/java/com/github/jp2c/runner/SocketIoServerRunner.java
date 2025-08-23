package com.github.jp2c.runner;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.room.controller.RoomController;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketIoServerRunner {
    private final SocketIOServer server;
    private final RoomController roomController;

    @PostConstruct
    public void start() {
       roomController.registerListeners(server);
    }
}
