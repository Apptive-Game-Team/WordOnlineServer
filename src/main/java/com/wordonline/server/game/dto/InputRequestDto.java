package com.wordonline.server.game.dto;

import lombok.Data;

import java.util.List;

@Data
public class InputRequestDto {
    private final String type = "useMagic";
    private final List<String> cards;
}
