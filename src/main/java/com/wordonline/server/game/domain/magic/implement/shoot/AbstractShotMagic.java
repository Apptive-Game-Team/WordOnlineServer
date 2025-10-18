package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public abstract class AbstractShotMagic extends Magic {

    private final PrefabType prefabType;

    public AbstractShotMagic(PrefabType prefabType) {
        this.prefabType = prefabType;
    }

    @Override
    public void run(GameLoop gameLoop, Master master, Vector3 position) {
        GameObject gameObject = new GameObject(
                getMaster(master),
                prefabType,
                GameConfig.PLAYER_POSITION.get(master),
                gameLoop);
        gameObject.getComponent(Shot.class).setTarget(position.toVector2());
    }

    protected Master getMaster(Master master) {
        return master;
    }
}
