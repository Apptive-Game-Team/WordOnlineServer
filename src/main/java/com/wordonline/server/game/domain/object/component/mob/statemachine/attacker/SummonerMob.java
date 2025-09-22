package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.dto.Master;

public class SummonerMob extends BehaviorMob {

    public SummonerMob(
            GameObject gameObject,
            int maxHp,
            float speed,
            int targetMask,
            float attackInterval,
            float attackRange,
            PrefabType prefabType) {
        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, (target) -> {
            new GameObject(target, Master.None, prefabType);
            return true;
        });
    }
}
