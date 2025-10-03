package com.github.jp2c.room.controller;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.github.jp2c.annotation.SocketEvent;
import com.github.jp2c.room.dto.RoomJoinOrLeaveRequest;
import com.github.jp2c.room.dto.RoomListResponse;
import com.github.jp2c.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @SocketEvent(value = "join-room",
        summary = "방 참가",
        description = "유저를 방에 참가시킵니다.",
        payload = RoomJoinOrLeaveRequest.class
    )
    public void handleJoinRoom(SocketIOClient client, RoomJoinOrLeaveRequest request, AckRequest ackRequest) {
        roomService.joinRoom(client, request, ackRequest);
    }

    @SocketEvent(value = "leave-room",
        summary = "방 나가기",
        description = "유저를 방에서 나가게 합니다..",
        payload = RoomJoinOrLeaveRequest.class
    )
    public void handleLeaveRoom(SocketIOClient client, RoomJoinOrLeaveRequest request, AckRequest ackRequest) {
        roomService.leaveRoom(client, request, ackRequest);
    }

    @SocketEvent(value = "get-all-rooms",
        summary = "현재 존재하는 Public Room 목록 찾기",
        response = RoomListResponse.class
    )
    public void handleGetAllRooms(AckRequest ackRequest) {
        roomService.getAllRooms(ackRequest);
    }
}
