package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;

import java.util.Optional;

public final class CombatRange {

    private CombatRange() {
    }

    public static boolean contains(GameObject source, GameObject target, double range) {
        return verticalDistance(source, target) <= range
                && horizontalEdgeDistance(source, target) <= range;
    }

    public static double horizontalEdgeDistance(GameObject source, GameObject target) {
        double centerDistance = source.getPosition().grounded()
                .distance(target.getPosition().grounded());
        return Math.max(0d, centerDistance - radius(source) - radius(target));
    }

    public static double verticalDistance(GameObject source, GameObject target) {
        return Math.abs(source.getPosition().getY() - target.getPosition().getY());
    }

    private static double radius(GameObject gameObject) {
        Optional<CircleCollider> collider = gameObject.getFirstCircleCollider(false);
        if (collider == null) {
            return 0d;
        }
        return collider
                .map(CircleCollider::getRadius)
                .orElse(0f);
    }
}
