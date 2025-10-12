package com.github.jp2c.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomJoinOrLeaveResponse {
    private String roomName;
    private String nickname;
}
