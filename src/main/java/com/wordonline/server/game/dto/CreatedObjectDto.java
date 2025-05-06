package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.Position;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatedObjectDto {
    private final int id;
    private final String type;
    private final Position position;
}
