package com.wordonline.server.game.domain.object.component.physic;


import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;

public abstract class Collider {
    protected final RigidBody rigidBody;
    protected final GameObject gameObject;

    public abstract boolean isCollidingWish(Collider collider);

    public Vector2 getPosition() {
        return gameObject.getPosition();
    }

    protected Collider(GameObject gameObject) {
        rigidBody = gameObject.getComponent(RigidBody.class);
        this.gameObject = gameObject;
    }
}
