package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class CraterSpawner extends MagicComponent {
    private final float attackInterval;
    private float elapsed;

    public CraterSpawner(GameObject gameObject, float attackInterval) {
        super(gameObject);
        this.attackInterval = Math.max(0.05f, attackInterval);
    }

    @Override
    public void update() {
        elapsed += getGameContext().getDeltaTime();
        while (elapsed >= attackInterval) {
            elapsed -= attackInterval;
            spawnEmber();
        }
    }

    private void spawnEmber() {
        new GameObject(gameObject.getMaster(), PrefabType.CraterEmber, gameObject.getPosition(), getGameContext());
    }
}
