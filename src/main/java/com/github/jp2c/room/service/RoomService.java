package com.github.jp2c.room.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.common.dto.CommonErrorResponse;
import com.github.jp2c.common.dto.CommonResponse;
import com.github.jp2c.exception.BadRequestException;
import com.github.jp2c.room.dto.RoomJoinOrLeaveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final SocketIOServer server;

    public void joinRoom(SocketIOClient client, RoomJoinOrLeaveRequest req, AckRequest ackRequest) {
        String nickname = client.get("nickname");
        if (server.getRoomOperations(req.getRoomName()).getClients().stream().anyMatch(
            socket -> Objects.equals(socket.get("nickname"), nickname)
        )) {
            String errorMessage = String.format("사용자 '%s'은(는) 이미 방에 존재합니다.", nickname);
            throw new BadRequestException(errorMessage);
        }
        client.joinRoom(req.getRoomName());
        log.info("{} joined room: {}", nickname, req.getRoomName());

        client.getNamespace().getRoomOperations(req.getRoomName())
            .sendEvent("room-joined", req);

        ackRequest.sendAckData(new CommonResponse<>(null));
    }

    public void leaveRoom(SocketIOClient client, RoomJoinOrLeaveRequest req, AckRequest ackRequest) {
        String nickname = client.get("nickname");

        if (client.getAllRooms().stream().noneMatch(roomName -> Objects.equals(roomName, req.getRoomName()))) {
            throw new BadRequestException("방에 참가하고 있지 않습니다.");
        }

        client.leaveRoom(req.getRoomName());
        log.info("{} left room: {}", nickname, req.getRoomName());

        client.getNamespace().getRoomOperations(req.getRoomName())
            .sendEvent("room-left", req);

        ackRequest.sendAckData(new CommonResponse<>(null));
    }

    public void getAllRooms(AckRequest ackRequest) {
        Set<String> allRooms = server.getAllClients().stream()
            .flatMap(client -> client.getAllRooms().stream())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
        ackRequest.sendAckData(new CommonResponse<>(allRooms));
    }
}
