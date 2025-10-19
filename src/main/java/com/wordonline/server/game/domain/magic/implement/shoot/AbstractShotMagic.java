package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public abstract class AbstractShotMagic extends Magic {

    private final PrefabType prefabType;

    public AbstractShotMagic(PrefabType prefabType) {
        super(CardType.Shoot);
        this.prefabType = prefabType;
    }

    @Override
    public void run(GameLoop gameLoop, Master master, Vector3 position) {
        PrefabInitializer prefabInitializer = PrefabProvider.get(prefabType);
        GameObject gameObject = new GameObject(
                getMaster(master),
                prefabInitializer,
                GameConfig.PLAYER_POSITION.get(master),
                gameLoop);
        gameObject.getComponent(Shot.class).setTarget(position.toVector2());
    }

    protected Master getMaster(Master master) {
        return master;
    }
}