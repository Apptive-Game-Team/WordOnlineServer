package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;


public abstract class Magic {

    public long id;
    public CardType magicType;

    public abstract void run(GameLoop gameLoop, Master master, Vector3 position);
}
