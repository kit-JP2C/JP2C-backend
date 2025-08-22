package com.github.maitmus.projectmj.room.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.maitmus.projectmj.common.dto.CommonErrorResponse;
import com.github.maitmus.projectmj.room.dto.RoomJoinOrLeaveRequest;
import com.github.maitmus.projectmj.room.dto.RoomListWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {
    public void joinRoom(SocketIOClient client, SocketIOServer server, RoomJoinOrLeaveRequest req, AckRequest ackRequest) {
        if (server.getRoomOperations(req.getRoomName()).getClients().stream().anyMatch(
            socket -> Objects.equals(socket.get("username"), req.getUsername())
        )) {
            String errorMessage = String.format("사용자 '%s'은(는) 이미 방에 존재합니다.", req.getUsername());
            log.error("Error on room '{}' : {}", req.getRoomName(), errorMessage);
            ackRequest.sendAckData(new CommonErrorResponse(errorMessage));
            return;
        }
        client.joinRoom(req.getRoomName());
        log.info("{} joined room: {}", req.getUsername(), req.getRoomName());

        client.set("username", req.getUsername());

        client.getNamespace().getRoomOperations(req.getRoomName())
            .sendEvent("room-joined", req);
    }

    public void leaveRoom(SocketIOClient client, RoomJoinOrLeaveRequest req) {
        client.leaveRoom(req.getRoomName());
        log.info("{} left room: {}", client.get("username"), req.getRoomName());

        client.del("username");

        client.getNamespace().getRoomOperations(req.getRoomName())
            .sendEvent("room-left", req);
    }

    public void getAllRooms(SocketIOServer server, AckRequest ackRequest) {
        Set<String> allRooms = server.getAllClients().stream()
            .flatMap(client -> client.getAllRooms().stream())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
        ackRequest.sendAckData(new RoomListWrapper(allRooms));
    }
}
