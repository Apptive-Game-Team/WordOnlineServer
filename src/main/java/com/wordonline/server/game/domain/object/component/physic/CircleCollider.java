package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import lombok.Getter;

@Getter
public class CircleCollider extends Collider {

    private final float radius;

    public CircleCollider(GameObject gameObject, float radius) {
        super(gameObject);
        this.radius = radius;
    }

    @Override
    public boolean isCollidingWish(Collider collider) {
        if (collider instanceof CircleCollider circleCollider) {
            float distance = (float) circleCollider.getPosition().distance(getPosition());
            return (distance < circleCollider.radius + radius);
        }

        if (collider instanceof EdgeCollider) {
            return collider.isCollidingWish(this);
        }

        return false;
    }
}
