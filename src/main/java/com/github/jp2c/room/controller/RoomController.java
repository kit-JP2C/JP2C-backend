package com.github.jp2c.room.controller;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.annotation.SocketEvent;
import com.github.jp2c.room.dto.RoomJoinOrLeaveRequest;
import com.github.jp2c.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @SocketEvent("join-room")
    public void handleJoinRoom(SocketIOClient client, RoomJoinOrLeaveRequest request, AckRequest ackRequest) {
        roomService.joinRoom(client, request, ackRequest);
    }

    @SocketEvent("leave-room")
    public void handleLeaveRoom(SocketIOClient client, RoomJoinOrLeaveRequest request, AckRequest ackRequest) {
        roomService.leaveRoom(client, request, ackRequest);
    }

    @SocketEvent("get-all-rooms")
    public void handleGetAllRooms(AckRequest ackRequest) {
        roomService.getAllRooms(ackRequest);
    }
}
