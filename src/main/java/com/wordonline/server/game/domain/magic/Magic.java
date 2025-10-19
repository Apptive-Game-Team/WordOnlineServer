package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Magic {

    public long id;
    public final CardType magicType;

    public abstract void run(GameContext gameContext, Master master, Vector3 position);
}
