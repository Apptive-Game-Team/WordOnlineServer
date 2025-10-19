package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public abstract class AbstractSummonMagic extends Magic {

    private final PrefabType prefabType;

    public AbstractSummonMagic(PrefabType prefabType) {
        super(CardType.Build);
        this.prefabType = prefabType;
    }

    @Override
    public void run(GameLoop gameLoop, Master master, Vector3 position) {
        PrefabInitializer prefabInitializer = PrefabProvider.get(prefabType);
        new GameObject(getMaster(master), prefabInitializer, position, gameLoop);
    }

    protected Master getMaster(Master master) {
        return master;
    }
}
