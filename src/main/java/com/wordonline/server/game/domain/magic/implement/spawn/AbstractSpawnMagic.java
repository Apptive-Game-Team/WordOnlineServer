package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public abstract class AbstractSpawnMagic extends Magic {

    private final PrefabType prefabType;

    public AbstractSpawnMagic(PrefabType prefabType) {
        this.prefabType = prefabType;
    }

    @Override
    public void run(GameLoop gameLoop, Master master, Vector3 position) {
        new GameObject(getMaster(master), prefabType, position, gameLoop);
        new GameObject(getMaster(master), prefabType, position.plus(0.5f, 0, 0), gameLoop);
        new GameObject(getMaster(master), prefabType, position.plus(-0.5f, 0, 0), gameLoop);
    }

    protected Master getMaster(Master master) {
        return master;
    }
}
