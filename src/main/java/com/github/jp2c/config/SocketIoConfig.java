package com.github.jp2c.config;

import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.github.jp2c.auth.entity.Account;
import com.github.jp2c.auth.service.AuthService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final AuthService authService;

    @Bean
    public SocketIOServer socketIoServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        config.setOrigin("*");
        config.setAuthorizationListener(data -> {
            String username = data.getSingleUrlParam("username");
            String password = data.getSingleUrlParam("password");

            Account account = authService.login(username, password);

            if (account != null) {
                Map<String, Object> storedParams = new HashMap<>();
                storedParams.put("id", account.getId());
                storedParams.put("nickname", account.getNickname());
                storedParams.put("username", account.getUsername());

                return new AuthorizationResult(true, storedParams);
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