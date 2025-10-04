package com.github.jp2c.config;

import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.github.jp2c.validator.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
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
    public SocketIOServer socketIoServer(JwtTokenValidator jwtTokenValidator) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        config.setOrigin("*");
        config.setAuthorizationListener(data -> {
            String accessToken = data.getSingleUrlParam("accessToken");

            Claims claims = jwtTokenValidator.validateToken(accessToken);


            Map<String, Object> storedParams = new HashMap<>();
            storedParams.put("id", claims.get("id"));
            storedParams.put("nickname", claims.get("nickname"));
            storedParams.put("username", claims.get("username"));

            return new AuthorizationResult(true, storedParams);
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