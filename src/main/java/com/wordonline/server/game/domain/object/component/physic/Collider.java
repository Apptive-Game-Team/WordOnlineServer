package com.wordonline.server.game.domain.object.component.physic;


import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;

public abstract class Collider {
    protected final RigidBody rigidBody;
    protected final GameObject gameObject;
    protected final boolean isTrigger;

    public abstract boolean isCollidingWish(Collider collider);

    public boolean isNotTrigger() {
        return !isTrigger;
    }

    public Vector3 getPosition() {
        return gameObject.getPosition();
    }

    public float getInvMass() {
        if (rigidBody == null) {
            return 0;
        }
        return rigidBody.getInvMass();
    }

    public Vector3 getVelocity() {
        if (rigidBody == null) {
            return Vector3.ZERO;
        }
        return rigidBody.getVelocity();
    }

    protected Collider(GameObject gameObject, boolean isTrigger) {
        rigidBody = gameObject.getComponent(RigidBody.class);
        this.isTrigger = isTrigger;
        this.gameObject = gameObject;
    }

    public abstract Vector3 getDisplacement(Collider colliderB);
}
