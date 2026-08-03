package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

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
            Vector3 summonPosition = target.getPosition();
            Vector3 offset = summonPosition.subtract(gameObject.getPosition()).grounded();
            if (offset.distance(Vector3.ZERO) > attackRange) {
                summonPosition = gameObject.getPosition()
                        .plus(offset.normalize().multiply(attackRange));
            }

            new GameObject(
                    gameObject.getMaster(),
                    prefabType,
                    summonPosition,
                    gameObject.getGameContext()
            );
            return true;
        });
    }
}
