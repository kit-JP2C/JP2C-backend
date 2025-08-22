package com.github.maitmus.projectmj.room.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.maitmus.projectmj.room.dto.RoomJoinOrLeaveRequest;
import com.github.maitmus.projectmj.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    public void registerListeners(SocketIOServer server) {
        server.addEventListener("join-room", RoomJoinOrLeaveRequest.class, (client, data, ackRequest) -> {
            roomService.joinRoom(client, server, data, ackRequest);
        });
        server.addEventListener("leave-room", RoomJoinOrLeaveRequest.class, (client, data, _) -> {
            roomService.leaveRoom(client, data);
        });
        server.addEventListener("get-all-rooms", Void.class, (_, _, ackRequest) -> {
            roomService.getAllRooms(server, ackRequest);
        });
    }
}
