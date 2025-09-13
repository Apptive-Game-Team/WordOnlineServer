package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public abstract class Magic {
    public final CardType magicType;
    public abstract void run(GameLoop gameLoop);
}

