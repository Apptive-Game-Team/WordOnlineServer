package com.wordonline.server.game.domain;

import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Status;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameObject {
    private final int id;
    private Type type;
    private Status status;
    private Effect effect;
    private final Position position;
}
