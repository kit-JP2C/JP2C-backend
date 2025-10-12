package com.github.jp2c.common.clientKeys;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.Getter;

public final class ClientKey<T> {
    @Getter
    private final String key;
    private final Class<T> type;

    public ClientKey(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    public T get(SocketIOClient client) {
        if (isNotInitialized(client)) {
            throw new IllegalStateException("Client is not initialized");
        }

        Object value = client.get(key);
        if (value == null) {
            throw new IllegalStateException("Client key '" + key + "' not initialized.");
        }
        return type.cast(value);
    }

    public void set(SocketIOClient client, T value) {
        client.set(key, value);
    }

    public void reset(SocketIOClient client) {
        if (isNotInitialized(client)) {
            throw new IllegalStateException("Client is not initialized");
        }

        client.del(key);
    }

    public boolean isNotInitialized(SocketIOClient client) {
        return client.get(key) == null;
    }
}
