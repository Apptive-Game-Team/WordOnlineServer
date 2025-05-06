package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.Position;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreatedObjectDto {
    private final int id;
    private final String type;
    private final Position position;
    private final Master master;
}
