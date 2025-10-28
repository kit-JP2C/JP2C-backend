package com.github.jp2c.room.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.common.clientKeys.ClientKeys;
import com.github.jp2c.common.dto.CommonResponse;
import com.github.jp2c.exception.BadRequestException;
import com.github.jp2c.point.service.PointService;
import com.github.jp2c.room.dto.ReadyOrUnreadyRequest;
import com.github.jp2c.room.dto.ReadyOrUnreadyResponse;
import com.github.jp2c.room.dto.RoomJoinOrLeaveRequest;
import com.github.jp2c.room.dto.RoomJoinOrLeaveResponse;
import com.github.jp2c.room.dto.RoomListResponse;
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
    private final PointService pointService;

    public void joinRoom(SocketIOClient client, RoomJoinOrLeaveRequest req) {
        String nickname = ClientKeys.NICKNAME.get(client);
        if (server.getRoomOperations(req.getRoomName()).getClients().stream().anyMatch(
            socket -> Objects.equals(ClientKeys.NICKNAME.get(socket), nickname)
        )) {
            String errorMessage = String.format("사용자 '%s'은(는) 이미 방에 존재합니다.", nickname);
            throw new BadRequestException(errorMessage);
        }
        client.joinRoom(req.getRoomName());
        log.info("{} joined room: {}", nickname, req.getRoomName());

        client.getNamespace().getRoomOperations(req.getRoomName())
            .sendEvent("room-joined", new RoomJoinOrLeaveResponse(
                req.getRoomName(),
                ClientKeys.NICKNAME.get(client)
            ));

        pointService.initializePoint(client);
    }

    public void leaveRoom(SocketIOClient client, RoomJoinOrLeaveRequest req) {
        String nickname = ClientKeys.NICKNAME.get(client);

        if (client.getAllRooms().stream().noneMatch(roomName -> Objects.equals(roomName, req.getRoomName()))) {
            throw new BadRequestException("방에 참가하고 있지 않습니다.");
        }

        client.leaveRoom(req.getRoomName());
        log.info("{} left room: {}", nickname, req.getRoomName());

        client.getNamespace().getRoomOperations(req.getRoomName())
            .sendEvent("room-left", new RoomJoinOrLeaveResponse(
                req.getRoomName(),
                ClientKeys.NICKNAME.get(client)
            ));
    }

    public void getAllRooms(AckRequest ackRequest) {
        Set<String> allRooms = getAllRoomLists();
        ackRequest.sendAckData(new CommonResponse<>(new RoomListResponse(allRooms)));
    }

    public Set<String> getAllRoomLists() {
        return server.getAllClients().stream()
            .flatMap(client -> client.getAllRooms().stream())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    public void ready(SocketIOClient client, ReadyOrUnreadyRequest request) {
        String callerNickname = ClientKeys.NICKNAME.get(client);

        ClientKeys.IS_READY.set(client, true);

        log.info("{} ready in room: {}", callerNickname, request.getRoomName());

        client.getNamespace().getRoomOperations(request.getRoomName())
            .sendEvent("room-readied", new ReadyOrUnreadyResponse(callerNickname));
    }

    public void unready(SocketIOClient client, ReadyOrUnreadyRequest request) {
        String callerNickname = ClientKeys.NICKNAME.get(client);

        ClientKeys.IS_READY.set(client, false);

        log.info("{} unready in room: {}", callerNickname, request.getRoomName());

        client.getNamespace().getRoomOperations(request.getRoomName())
            .sendEvent("room-unreadied", new ReadyOrUnreadyResponse(callerNickname));
    }
}
