package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetRelation;

public class TitanFistExplosion extends Explode {

    public TitanFistExplosion(GameObject gameObject, int damage, float radius) {
        super(gameObject, damage, radius);
    }

    @Override
    protected void handleGameObject(GameObject targetObject) {
        if (TargetRelation.canAttack(gameObject, targetObject)) {
            super.handleGameObject(targetObject);
        }
    }
}
