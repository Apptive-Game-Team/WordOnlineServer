package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotTier;

/**
 * A floor on how often a bot may cast, for the persona that needs one.
 *
 * <p>Only the tutorial opponent is paced. A bot playing at full speed buries a new player who is
 * still reading their cards, and the fix cannot be "cast less when the player is behind": a pause
 * is the one thing a player reads as being humoured. So the interval is a constant floor the bot
 * keeps for the whole game, and the difficulty moves through what it casts instead.
 */
final class CastPacer {

    /** How long the hospitality bot waits between casts. */
    static final long HOSPITALITY_INTERVAL_MILLIS = 30_000L;

    private final long intervalMillis;

    // Written by the bot executor thread when a cast goes out, read by the loop thread when it
    // decides whether to submit a tick. Only one tick runs at a time, so a plain volatile is enough.
    private volatile long nextAllowedAtMillis;

    private CastPacer(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    static CastPacer forTier(BotTier tier) {
        return new CastPacer(tier == BotTier.HOSPITALITY ? HOSPITALITY_INTERVAL_MILLIS : 0L);
    }

    boolean allows(long nowMillis) {
        return intervalMillis == 0L || nowMillis >= nextAllowedAtMillis;
    }

    void recordCast(long nowMillis) {
        nextAllowedAtMillis = nowMillis + intervalMillis;
    }
}
