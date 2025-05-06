package com.wordonline.server.game.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@RequiredArgsConstructor
public class FrameInfoDto {
    private final String type = "frame";
    private final int updatedMana;
    private final CardInfoDto cards;
    private final ObjectsInfoDto objects;
}


