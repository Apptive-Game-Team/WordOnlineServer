package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.component.Damageable;

public class Spawner extends MagicComponent implements Damageable {
    public static final int SPAWN_INTERNAL = 2;

    private float counter = 0;
    private boolean isRunning = false;

    private int hp = 0;
    private int maxHp = 0;

    @Override
    public void update() {
        if (!isRunning) {
            return;
        }

        if (counter < SPAWN_INTERNAL) {
            counter += gameObject.getGameLoop().deltaTime;
        } else {
            counter = 0;
            new GameObject(gameObject, PrefabType.FireSlime);
        }
    }

    public Spawner(GameObject gameObject, int maxHp) {
        super(gameObject);
        isRunning = true;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    @Override
    public void onDamaged(AttackInfo attackInfo) {
        hp -= attackInfo.getDamage();
        if (hp <= 0) {
            gameObject.destroy();
        }
    }
}
