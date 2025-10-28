package com.github.jp2c.status.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.jp2c.common.dto.CommonResponse;
import com.github.jp2c.room.service.RoomService;
import com.github.jp2c.status.dto.CurrentSocketStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/socket-status")
@RequiredArgsConstructor
public class SocketStatusController {
    private final SocketIOServer server;
    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "현재 접속중인 클라이언트 수/방 수")
    public CommonResponse<?> getCurrentSocketStatus() {
        return new CommonResponse<>(new CurrentSocketStatusResponse(server.getAllClients().size(), roomService.getAllRoomLists().size()));
    }
}
