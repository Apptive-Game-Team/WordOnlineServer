package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import lombok.Getter;

@Getter
public class CircleCollider extends Collider {
    private final float radius;

    public CircleCollider(GameObject gameObject, float radius, boolean isTrigger) {
        super(gameObject, isTrigger);
        this.radius = radius;
    }

    @Override
    public boolean isCollidingWish(Colliderable collider) {
        if (collider instanceof CircleCollider circleCollider) {
            float distance = (float) circleCollider.getPosition().distance(getPosition());
            return (distance < circleCollider.radius + radius);
        }

        if (collider instanceof EdgeCollider) {
            return collider.isCollidingWish(this);
        }

        return false;
    }

    @Override
    public Vector3 getDisplacement(Collider collider) {
        if (collider instanceof CircleCollider circleCollider) {
            return getPosition().subtract(circleCollider.getPosition());
        }

        if (collider instanceof EdgeCollider edgeCollider) {
            return edgeCollider.getDisplacement(this).multiply(-1);
        }

        return null;
    }
}
