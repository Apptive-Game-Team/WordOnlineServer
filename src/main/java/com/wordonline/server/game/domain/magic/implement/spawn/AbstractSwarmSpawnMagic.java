package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;

public abstract class AbstractSwarmSpawnMagic extends Magic {

    private final PrefabType prefabType;

    public AbstractSwarmSpawnMagic(PrefabType prefabType) {
        super(CardType.Spawn);
        this.prefabType = prefabType;
    }

    @Override
    public void run(GameContext gameContext, Master master, Vector3 position) {
        new GameObject(getMaster(master), prefabType, position, gameContext);
        new GameObject(getMaster(master), prefabType, position.plus(0.5f, 0, 0), gameContext);
        new GameObject(getMaster(master), prefabType, position.plus(-0.5f, 0, 0), gameContext);
    }

    protected Master getMaster(Master master) {
        return master;
    }
}