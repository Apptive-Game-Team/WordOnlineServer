package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.Vector3;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CircleCollidingHelper implements Colliderable {
    private final float radius;
    private final Vector3 position;

    @Override
    public Vector3 getPosition() {
        return position;
    }

    @Override
    public boolean isCollidingWish(Colliderable collider) {
        if (collider instanceof CircleCollider circleCollider) {
            float distance = (float) circleCollider.getPosition().distance(getPosition());
            return (distance < circleCollider.getRadius() + radius);
        }

        if (collider instanceof EdgeCollider) {
            return collider.isCollidingWish(this);
        }

        return false;
    }
}
