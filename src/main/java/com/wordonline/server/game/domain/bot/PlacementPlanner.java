package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

import java.util.Random;

/**
 * Chooses where a summon or structure should be dropped.
 *
 * <p>Placement used to be a uniformly random point in the half-disc facing the enemy, which
 * scattered blockers across lanes the fight was not in. The planner instead puts the object in the
 * contested lane: ahead of the bot when it is being pushed, so it intercepts, and further forward
 * when the field is clear, so it starts advancing.
 */
public final class PlacementPlanner {

    /** Fraction of the gap to the nearest threat at which a blocker is dropped. */
    static final double INTERCEPT_GAP_FRACTION = 0.6;

    /** Fraction of the cast range used when there is nothing to intercept. */
    static final double ADVANCE_RANGE_FRACTION = 0.9;

    /** Lane spread applied when advancing, so repeated summons do not stack on one point. */
    static final double ADVANCE_LANE_JITTER = 1.5;

    private PlacementPlanner() {
    }

    public static Vector3 plan(Vector3 casterPosition,
                               double castRange,
                               Master botSide,
                               ThreatAssessment threats,
                               Random random) {
        float forward = forwardDirection(botSide);
        float laneZ = threats.contestedLaneZ();

        double offset;
        if (threats.hasThreats()) {
            double gap = threats.threats().getFirst().distanceToDefendedPosition();
            offset = Math.min(castRange, gap * INTERCEPT_GAP_FRACTION);
        } else {
            offset = castRange * ADVANCE_RANGE_FRACTION;
            laneZ += (float) ((random.nextDouble() * 2 - 1) * ADVANCE_LANE_JITTER);
        }

        Vector3 desired = new Vector3(
                casterPosition.getX() + forward * (float) offset,
                casterPosition.getY(),
                laneZ);
        return clampToRange(casterPosition, clampToMapBounds(desired), castRange);
    }

    private static float forwardDirection(Master botSide) {
        return switch (botSide) {
            case LeftPlayer -> 1f;
            case RightPlayer -> -1f;
            default -> throw new IllegalArgumentException("Unknown botSide: " + botSide);
        };
    }

    private static Vector3 clampToMapBounds(Vector3 position) {
        return new Vector3(
                Math.clamp(position.getX(), 0f, (float) GameConfig.WIDTH),
                position.getY(),
                Math.clamp(position.getZ(), 0f, (float) GameConfig.HEIGHT));
    }

    private static Vector3 clampToRange(Vector3 origin, Vector3 position, double range) {
        double distance = origin.distance(position);
        if (distance <= range || distance == 0) {
            return position;
        }
        return origin.plus(position.subtract(origin).normalize().multiply((float) range));
    }
}
