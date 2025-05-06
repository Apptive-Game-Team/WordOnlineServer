package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.Position;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdatedObjectDto {
    private final int id;
    private final Status type;
    private final Effect effect;
    private final Position position;
}
