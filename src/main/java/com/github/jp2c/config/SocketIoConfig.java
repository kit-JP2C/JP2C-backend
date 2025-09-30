package com.github.jp2c.config;

import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class SocketIoConfig {
    @Value("${socketio.server.hostname}")
    private String hostname;

    @Value("${socketio.server.port}")
    private int port;

    private SocketIOServer server;

    private final Optional<RedissonClient> redissonClient;

    @Bean
    public SocketIOServer socketIoServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        config.setOrigin("*");
        config.setAuthorizationListener(data -> {
            String username = data.getSingleUrlParam("username");
            String password = data.getSingleUrlParam("password");

            // TODO: DB 조회 or 로그인 서비스 호출
            if ("test".equals(username) && "1234".equals(password)) {
                return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
            }
            return AuthorizationResult.FAILED_AUTHORIZATION;
        });

        redissonClient.ifPresent(client -> config.setStoreFactory(new RedissonStoreFactory(client)));

        server = new SocketIOServer(config);
        server.start();
        return server;
    }

    @PreDestroy
    public void stopSocketIoServer() {
        if (server != null) {
            server.stop();
        }
    }
}