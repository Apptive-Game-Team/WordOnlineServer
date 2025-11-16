package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import lombok.Getter;

@Getter
public class CylinderCollider extends Collider {

    private final float radius;
    private final float height;

    public CylinderCollider(GameObject gameObject, float radius, float height, boolean isTrigger) {
        super(gameObject, isTrigger);
        this.radius = radius;
        this.height = height;
    }

    @Override
    public boolean isCollidingWish(Collider collider) {
        if (collider instanceof CircleCollider circleCollider) {
        }

        if (collider instanceof EdgeCollider) {
        }

        return false;
    }

    @Override
    public Vector3 getDisplacement(Collider collider) {
        if (collider instanceof CircleCollider circleCollider) {
        }

        if (collider instanceof EdgeCollider edgeCollider) {
        }

        return null;
    }
}
