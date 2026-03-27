package com.wordonline.server.game.domain.magic.implement.spawn;

import java.util.Random;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;

public abstract class AbstractSwarmSpawnMagic extends Magic {

    private final PrefabType prefabType;
    private static final Random random = new java.util.Random();

    public AbstractSwarmSpawnMagic(PrefabType prefabType) {
        super(CardType.Spawn);
        this.prefabType = prefabType;
    }

    // default
    protected int getNum() {
        return 3;
    }

    @Override
    public void run(GameContext gameContext, Master master, Vector3 position) {
        Master currentMaster = getMaster(master);
        int count = getNum();

        float range = 1.0f;

        for (int i = 0; i < count; i++) {
            float randomX = (random.nextFloat() * 2 - 1) * range;
            float randomY = (random.nextFloat() * 2 - 1) * range;

            Vector3 spawnPosition = position.plus(randomX, randomY, 0);

            new GameObject(currentMaster, prefabType, spawnPosition, gameContext);
        }

    }

    protected Master getMaster(Master master) {
        return master;
    }
}
