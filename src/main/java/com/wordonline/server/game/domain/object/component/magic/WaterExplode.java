package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.ForcedMovement;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;

public class WaterExplode extends Explode {

    private final float zForce;

    public WaterExplode(GameObject gameObject,
            int damage,
            float radius,
            float zForce) {
        super(gameObject, damage, radius);
        this.zForce = zForce;
    }

    @Override
    protected void handleGameObject(GameObject targetObject) {
        super.handleGameObject(targetObject);

        targetObject.getComponentOptional(ZPhysics.class)
                .ifPresent(zPhysics -> zPhysics.addImpulseZ(
                        zForce * ForcedMovement.massMultiplier(targetObject)));
    }
}
