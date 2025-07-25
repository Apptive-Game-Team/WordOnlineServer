package com.wordonline.server.game.domain.object.component.physic;


import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;

public abstract class Collider {
    protected final RigidBody rigidBody;
    protected final GameObject gameObject;
    protected final boolean isTrigger;

    public abstract boolean isCollidingWish(Collider collider);

    public boolean isNotTrigger() {
        return !isTrigger;
    }

    public Vector2 getPosition() {
        return gameObject.getPosition();
    }

    public float getInvMess() {
        if (rigidBody == null) {
            return 0;
        }
        return rigidBody.getInvMess();
    }

    public Vector2 getVelocity() {
        if (rigidBody == null) {
            return Vector2.ZERO;
        }
        return rigidBody.getVelocity();
    }

    protected Collider(GameObject gameObject, boolean isTrigger) {
        rigidBody = gameObject.getComponent(RigidBody.class);
        this.isTrigger = isTrigger;
        this.gameObject = gameObject;
    }

    public abstract Vector2 getDisplacement(Collider colliderB);
}
