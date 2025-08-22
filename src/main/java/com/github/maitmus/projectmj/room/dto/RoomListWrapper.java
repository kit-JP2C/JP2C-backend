package com.github.maitmus.projectmj.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomListWrapper {
    Set<String> rooms;
}
