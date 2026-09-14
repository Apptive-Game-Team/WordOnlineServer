package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * A straight attack that hits everything along a line instead of one target at its end.
 *
 * <p>The width is measured outward from the line, and a candidate's own collider radius is added
 * to it, so a bulky body is hit as soon as its edge touches the beam rather than only when its
 * centre does.
 *
 * <p>Height is ignored. The caller passes a grounded line and a beam reaches ground and air alike,
 * which is why neither {@code start} nor {@code end} carries a meaningful Y.
 */
public final class Beam {

    private Beam() {
    }

    /**
     * Every object {@code source} may attack that the line from {@code start} to {@code end}
     * passes within {@code width} of, in no particular order.
     */
    public static List<GameObject> targetsAlong(GameObject source, Vector3 start, Vector3 end, float width) {
        List<GameObject> targets = new ArrayList<>();

        for (GameObject candidate : source.getGameContext().getActiveGameObjects()) {
            if (!TargetRelation.canAttack(source, candidate)) {
                continue;
            }

            if (candidate.getComponent(Damageable.class) == null) {
                continue;
            }

            float candidateRadius = candidate.getFirstCircleCollider()
                    .map(collider -> collider.getRadius())
                    .orElse(0f);
            if (distanceToSegment(candidate.getPosition().grounded(), start, end) > width + candidateRadius) {
                continue;
            }

            targets.add(candidate);
        }

        return targets;
    }

    public static double distanceToSegment(Vector3 point, Vector3 start, Vector3 end) {
        Vector3 segment = end.subtract(start);
        float lengthSquared = segment.dot(segment);
        if (lengthSquared == 0f) {
            return point.distance(start);
        }

        float progress = Math.clamp(point.subtract(start).dot(segment) / lengthSquared, 0f, 1f);
        Vector3 closest = start.plus(segment.multiply(progress));
        return point.distance(closest);
    }
}
