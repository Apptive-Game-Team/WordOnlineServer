package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * What the bot knows about hostile objects on the field this tick: which ones can still be hit,
 * how much pressure they put on the position the bot defends, and which lane that pressure sits in.
 *
 * <p>Instances are immutable snapshots, built from the frame snapshot the loop thread handed the
 * bot, so nothing here reads live game state.
 */
public final class ThreatAssessment {

    /** Beyond this distance an enemy no longer contributes to defensive pressure. */
    static final double PRESSURE_HORIZON = 9.0;

    /** Weighted threat count at which pressure is considered maximal. */
    static final double PRESSURE_SATURATION = 3.0;

    private final List<EnemyThreat> threats;
    private final Vector3 defendedPosition;

    private ThreatAssessment(List<EnemyThreat> threats, Vector3 defendedPosition) {
        this.threats = threats;
        this.defendedPosition = defendedPosition;
    }

    public static ThreatAssessment observe(List<BotVisibleObject> gameObjects,
                                           Master enemySide,
                                           Vector3 defendedPosition,
                                           int enemyPlayerHp) {
        List<EnemyThreat> observed = new ArrayList<>();
        for (BotVisibleObject gameObject : gameObjects) {
            if (gameObject.master() != enemySide || !gameObject.targetable()) {
                continue;
            }

            Vector3 position = gameObject.position();
            boolean playerCore = gameObject.type() == PrefabType.Player;
            observed.add(new EnemyThreat(
                    gameObject.id(),
                    position,
                    playerCore ? enemyPlayerHp : gameObject.hp(),
                    position.distance(defendedPosition),
                    playerCore));
        }
        observed.sort(Comparator.comparingDouble(EnemyThreat::distanceToDefendedPosition));
        return new ThreatAssessment(List.copyOf(observed), defendedPosition);
    }

    public List<EnemyThreat> threats() {
        return threats;
    }

    public boolean hasThreats() {
        return !threats.isEmpty();
    }

    /**
     * How hard the bot is being pushed, from 0 (nothing nearby) to 1 (several enemies at the door).
     */
    public double pressure() {
        double weighted = 0;
        for (EnemyThreat threat : threats) {
            weighted += proximityWeight(threat);
        }
        return Math.clamp(weighted / PRESSURE_SATURATION, 0.0, 1.0);
    }

    /**
     * The lane the incoming pressure is concentrated in, as a z coordinate. Falls back to the
     * defended position's own lane when nothing is close enough to matter.
     */
    public float contestedLaneZ() {
        double weightSum = 0;
        double weightedZ = 0;
        for (EnemyThreat threat : threats) {
            double weight = proximityWeight(threat);
            weightSum += weight;
            weightedZ += weight * threat.position().getZ();
        }
        return weightSum == 0 ? defendedPosition.getZ() : (float) (weightedZ / weightSum);
    }

    private static double proximityWeight(EnemyThreat threat) {
        if (threat.playerCore()) {
            return 0;
        }
        return Math.clamp(1.0 - threat.distanceToDefendedPosition() / PRESSURE_HORIZON, 0.0, 1.0);
    }
}
