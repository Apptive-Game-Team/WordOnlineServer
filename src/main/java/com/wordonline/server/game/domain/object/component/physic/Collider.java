package com.wordonline.server.game.domain.object.component.physic;


import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;

import lombok.Getter;

public abstract class Collider implements Colliderable {
    protected final GameObject gameObject;
    @Getter
    protected final boolean isTrigger;

    public boolean isNotTrigger() {
        return !isTrigger;
    }
    public Vector3 getPosition() {
        return gameObject.getPosition();
    }

    // RigidBody is resolved lazily: prefabs may register it through the deferred
    // addComponent queue after this collider is constructed
    public float getInvMass() {
        RigidBody rigidBody = gameObject.getComponent(RigidBody.class);
        if (rigidBody == null) {
            return 0;
        }
        return rigidBody.getInvMass();
    }

    public Vector3 getVelocity() {
        RigidBody rigidBody = gameObject.getComponent(RigidBody.class);
        if (rigidBody == null) {
            return Vector3.ZERO;
        }
        return rigidBody.getVelocity();
    }

    protected Collider(GameObject gameObject, boolean isTrigger) {
        this.isTrigger = isTrigger;
        this.gameObject = gameObject;
    }

    public abstract Vector3 getDisplacement(Collider colliderB);
}
