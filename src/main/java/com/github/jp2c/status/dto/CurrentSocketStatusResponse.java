package com.github.jp2c.status.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSocketStatusResponse {
    private int clients;
    private int rooms;
}
