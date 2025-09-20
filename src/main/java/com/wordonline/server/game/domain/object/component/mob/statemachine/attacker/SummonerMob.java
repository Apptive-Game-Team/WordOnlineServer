package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;

public class SummonerMob extends BehaviorMob {

    public SummonerMob(
            GameObject gameObject,
            int maxHp,
            float speed,
            float attackInterval,
            float attackRange,
            PrefabType prefabType) {
        super(gameObject, maxHp, speed, attackInterval, attackRange, (target) -> {
            new GameObject(gameObject, prefabType);
            return true;
        });
    }
}
