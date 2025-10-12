package com.github.jp2c.point.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.jp2c.common.clientKeys.ClientKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    public void initializePoint(SocketIOClient client) {
        ClientKeys.POINT.set(client, 25000);
    }

    public void resetPoint(SocketIOClient client) {
        ClientKeys.POINT.set(client, 0);
    }

    public void increasePoint(SocketIOClient client, int point) {
        int currentPoint = ClientKeys.POINT.get(client);
        ClientKeys.POINT.set(client, currentPoint + point);
    }

    public void decreasePoint(SocketIOClient client, int point) {
        int currentPoint = ClientKeys.POINT.get(client);
        ClientKeys.POINT.set(client, currentPoint - point);
    }
}
