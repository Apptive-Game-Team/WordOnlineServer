package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.object.Vector3;

import java.util.List;
import java.util.Optional;

/**
 * Picks where an offensive spell should land.
 *
 * <p>Instead of always aiming at the closest enemy, every reachable enemy position is treated as a
 * candidate blast centre and scored by the damage the blast would actually convert: capped at each
 * covered target's remaining hit points, so a large spell is not wasted on a dying slime, and
 * weighted up when it reaches the enemy player, which is what ends the match.
 */
public final class BlastTargetSelector {

    /** Damage on the enemy player counts for more than damage on a summon: it wins the game. */
    static final double PLAYER_CORE_WEIGHT = 2.0;

    private BlastTargetSelector() {
    }

    /**
     * @param center         where to cast
     * @param expectedDamage weighted damage the blast is expected to convert
     * @param coveredCount   how many enemies fall inside the blast radius
     * @param lethalCount    how many of those the blast is expected to finish off
     */
    public record Impact(Vector3 center, double expectedDamage, int coveredCount, int lethalCount) {
    }

    public static Optional<Impact> bestImpact(List<EnemyThreat> threats,
                                              Vector3 castOrigin,
                                              double castRange,
                                              double blastRadius,
                                              double damagePerTarget) {
        Impact best = null;
        for (EnemyThreat candidate : threats) {
            if (candidate.position().distance(castOrigin) > castRange) {
                continue;
            }

            Impact impact = impactAt(candidate.position(), threats, blastRadius, damagePerTarget);
            if (best == null || isBetter(impact, best, castOrigin)) {
                best = impact;
            }
        }
        return Optional.ofNullable(best);
    }

    private static Impact impactAt(Vector3 center,
                                   List<EnemyThreat> threats,
                                   double blastRadius,
                                   double damagePerTarget) {
        double damage = 0;
        int covered = 0;
        int lethal = 0;
        for (EnemyThreat threat : threats) {
            if (threat.position().distance(center) > blastRadius) {
                continue;
            }
            covered++;
            double converted = Math.min(damagePerTarget, threat.hp());
            damage += threat.playerCore() ? converted * PLAYER_CORE_WEIGHT : converted;
            if (!threat.playerCore() && damagePerTarget >= threat.hp()) {
                lethal++;
            }
        }
        return new Impact(center, damage, covered, lethal);
    }

    private static boolean isBetter(Impact candidate, Impact current, Vector3 castOrigin) {
        if (candidate.expectedDamage() != current.expectedDamage()) {
            return candidate.expectedDamage() > current.expectedDamage();
        }
        if (candidate.coveredCount() != current.coveredCount()) {
            return candidate.coveredCount() > current.coveredCount();
        }
        // Equal value: hit the closer cluster first, it reaches the bot's own side sooner.
        return candidate.center().distance(castOrigin) < current.center().distance(castOrigin);
    }
}
