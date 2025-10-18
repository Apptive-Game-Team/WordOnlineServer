package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public abstract class AbstractSingleSpawnMagic extends Magic {

    private final PrefabType prefabType;

    public AbstractSingleSpawnMagic(PrefabType prefabType) {
        super(CardType.Spawn);
        this.prefabType = prefabType;
    }

    @Override
    public void run(GameLoop gameLoop, Master master, Vector3 position) {
        new GameObject(getMaster(master), prefabType, position, gameLoop);
    }

    protected Master getMaster(Master master) {
        return master;
    }
}
