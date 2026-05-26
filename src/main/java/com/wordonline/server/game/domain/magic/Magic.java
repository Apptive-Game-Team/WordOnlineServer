package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Magic {

    public long id;
    public final CardType magicType;

    public abstract void run(GameContext gameContext, Master master, Vector3 position);

    public void run(GameContext gameContext, Master master, Vector3 castOrigin, Vector3 position) {
        run(gameContext, master, position);
    }

}
