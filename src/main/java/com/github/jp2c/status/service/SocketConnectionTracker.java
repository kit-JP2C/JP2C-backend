package com.github.jp2c.status.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketConnectionTracker {
    private final RedissonClient redissonClient;
    private static final String SOCKET_CONNECTIONS_KEY = "socket:connections";

    public void onConnect(String sessionId) {
        redissonClient.getSet(SOCKET_CONNECTIONS_KEY).add(sessionId);
    }

    public void onDisconnect(String sessionId) {
        redissonClient.getSet(SOCKET_CONNECTIONS_KEY).remove(sessionId);
    }

    public void onJoin(SocketIOClient client, String room) {
        redissonClient.getSet("socket:rooms:" + room).add(client.getSessionId().toString());
    }

    public void onLeave(SocketIOClient client, String room) {
        redissonClient.getSet("socket:rooms:" + room).remove(client.getSessionId().toString());
    }

    public int getActiveClientCount() {
        return redissonClient.getSet(SOCKET_CONNECTIONS_KEY).size();
    }
}