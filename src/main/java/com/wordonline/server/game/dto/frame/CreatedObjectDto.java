package com.wordonline.server.game.dto.frame;

import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.dto.Master;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
// This class is used to send created object information to the client
public class CreatedObjectDto {
    private final int id;
    private final PrefabType type;
    private final Vector2 position;
    private final Master master;
}
