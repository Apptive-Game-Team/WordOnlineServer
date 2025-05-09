package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.object.Position;
import com.wordonline.server.game.domain.object.PrefabType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
// This class is used to send created object information to the client
public class CreatedObjectDto {
    private final int id;
    private final PrefabType type;
    private final Position position;
    private final Master master;
}
