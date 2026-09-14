package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.ObjectSummoningMagic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;

public abstract class AbstractSummonMagic extends Magic implements ObjectSummoningMagic {

    private final PrefabType prefabType;

    /**
     * 건물을 놓을 높이. null 이면 조준점의 y 를 그대로 쓴다. {@code cannon}, {@code tower},
     * {@code dragon_tower} 는 0 을 받아 바닥에 놓인다.
     */
    private final Float spawnHeight;

    public AbstractSummonMagic(PrefabType prefabType) {
        this(prefabType, null);
    }

    protected AbstractSummonMagic(PrefabType prefabType, Float spawnHeight) {
        super(CardType.Build);
        this.prefabType = prefabType;
        this.spawnHeight = spawnHeight;
    }

    @Override
    public void run(GameContext gameContext, Master master, Vector3 position) {
        new GameObject(getMaster(master), prefabType, summonPosition(position), gameContext);
    }

    private Vector3 summonPosition(Vector3 position) {
        if (spawnHeight == null) {
            return position;
        }
        return new Vector3(position.getX(), spawnHeight, position.getZ());
    }

    @Override
    public PrefabType summonedPrefab() {
        return prefabType;
    }

    protected Master getMaster(Master master) {
        return master;
    }
}
