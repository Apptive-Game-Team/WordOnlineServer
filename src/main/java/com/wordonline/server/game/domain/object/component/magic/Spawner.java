package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.component.mob.Mob;

public class Spawner extends Mob {
    public static final int SPAWN_INTERNAL = 2;

    private float counter = 0;
    private boolean isRunning = false;
    private PrefabInitializer prefabInitializer;

    @Override
    public void start() {

    }

    @Override
    public void update() {
        if (!isRunning) {
            return;
        }

        if (counter < SPAWN_INTERNAL) {
            counter += getGameContext().getDeltaTime();
        } else {
            counter = 0;
            new GameObject(gameObject, prefabInitializer);
            onDamaged(new AttackInfo(1, ElementType.NONE));
        }
    }

    @Override
    public void onDestroy() {

    }

    public Spawner(GameObject gameObject, int maxHp, PrefabInitializer prefabInitializer) {
        super(gameObject, maxHp, 0);
        this.prefabInitializer = prefabInitializer;
        isRunning = true;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    @Override
    public void onDamaged(AttackInfo attackInfo) {
        hp -= attackInfo.getDamage();
        getGameContext().updateGameObject(gameObject);
        if (hp <= 0) {
            gameObject.destroy();
        }
    }

    @Override
    public void onDeath() {

    }
}
