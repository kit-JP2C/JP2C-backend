package com.github.jp2c.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomJoinOrLeaveRequest {
    String username;
    String roomName;
}
