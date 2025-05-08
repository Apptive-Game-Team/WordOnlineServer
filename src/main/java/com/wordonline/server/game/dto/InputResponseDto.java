package com.wordonline.server.game.dto;

import lombok.Data;

@Data
public class InputResponseDto {
    private final String type = "magicValid";
    private final boolean valid;
    private final int updatedMana;
}
