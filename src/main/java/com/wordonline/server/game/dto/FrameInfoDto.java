package com.wordonline.server.game.dto;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
public class FrameInfoDto {
    private final String type = "frame";
    private final int updatedMana;
    private final CardInfoDto cards;
    private final ObjectsInfoDto objects;
}


