package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;

public final class ForcedMovement {

    private ForcedMovement() {
    }

    public static float massMultiplier(GameObject target) {
        RigidBody rigidBody = target.getComponent(RigidBody.class);
        return rigidBody == null ? 1f : massMultiplier(rigidBody.getMass());
    }

    public static float massMultiplier(int mass) {
        if (mass < 0) {
            return 0f;
        }
        return 1f / (float) Math.sqrt(Math.max(mass, 1));
    }
}
