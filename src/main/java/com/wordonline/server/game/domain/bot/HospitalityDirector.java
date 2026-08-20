package com.wordonline.server.game.domain.bot;

/**
 * How hard the hospitality bot leans into losing, one cast at a time.
 *
 * <p>The bot this drives is the opponent a new player meets at the end of the tutorial. It is meant
 * to lose, but never to look like it is being polite about it. Difficulty comes from <em>what</em>
 * it summons - a unit the player's board beats - and never from summoning less often. Standing
 * still is the one failure mode that reads as being humoured, and it costs more than losing the
 * fight would, which is why {@link CastDeadline} caps how long the bot may stay quiet rather than
 * how often it may act.
 *
 * <p>The dial it turns is {@code counterAggression}, whose negative half makes the brain prefer the
 * play the enemy board answers best. Full hospitality is a unit that walks straight into what the
 * player already has out; eased hospitality is a unit that is merely unremarkable, so a player who
 * is already ahead still has something to beat.
 */
public final class HospitalityDirector {

    /** Summon whatever the player's board answers best. */
    static final double FULL = -1.0;

    /** Still deliberately weak, but not a gift; used once the player is clearly ahead. */
    static final double EASED = -0.2;

    /** Between the two, for a board that is close. */
    static final double PARTIAL = -0.6;

    /** Unit lead at which the player is treated as clearly ahead on board. */
    static final int COMFORTABLE_LEAD = 2;

    /** Defensive pressure at which the player is treated as clearly ahead regardless of counts. */
    static final double COMFORTABLE_PRESSURE = 0.6;

    /**
     * @param playerUnitCount bodies the player has on the field
     * @param botUnitCount    bodies the bot has on the field
     * @param pressure        how hard the player is pushing, 0 to 1, from {@link ThreatAssessment}
     */
    public double aggression(int playerUnitCount, int botUnitCount, double pressure) {
        int lead = playerUnitCount - botUnitCount;

        // The player is not ahead on board. Hand the fight over: summon the thing their units
        // answer best, so the next trade goes their way.
        if (lead <= 0) {
            return FULL;
        }

        // The player is winning on their own. Easing off here is what keeps the win worth having -
        // a bot that only ever feeds is not an opponent.
        if (lead >= COMFORTABLE_LEAD || pressure >= COMFORTABLE_PRESSURE) {
            return EASED;
        }

        return PARTIAL;
    }
}
