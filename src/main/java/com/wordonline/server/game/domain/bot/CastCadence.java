package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotTier;

/**
 * How often the hospitality bot summons: once per interval, no more and no less.
 *
 * <p>Both halves matter and they were learned the hard way. Without a floor the bot summons on
 * every tick its mana allows, and a new player cannot clear a board that refills faster than they
 * can answer it - the opponent built to lose ends up winning comfortably. Without a ceiling the bot
 * can go quiet whenever it has nothing worth casting, and a player reads a still opponent as one
 * that is going easy on them, which costs more than losing the fight would.
 *
 * <p>So the interval is both. The bot may not act before it elapses, and once it has, the bot acts
 * whether or not it likes its options.
 *
 * <p>Every other tier is unpaced: they are trying to win, and neither failure mode applies.
 */
final class CastCadence {

    /** One summon a minute for the tutorial opponent. */
    static final long HOSPITALITY_INTERVAL_MILLIS = 60_000L;

    /** Zero means the persona casts as often as it likes. */
    private final long intervalMillis;

    // Written by the bot executor thread when a cast goes out, read by the loop thread when it
    // decides whether to submit a tick. Only one tick runs at a time, so a plain volatile is enough.
    private volatile long lastCastAtMillis;

    private CastCadence(long intervalMillis, long startedAtMillis) {
        this.intervalMillis = intervalMillis;
        this.lastCastAtMillis = startedAtMillis;
    }

    static CastCadence forTier(BotTier tier, long startedAtMillis) {
        return new CastCadence(
                tier == BotTier.HOSPITALITY ? HOSPITALITY_INTERVAL_MILLIS : 0L,
                startedAtMillis);
    }

    /** Whether the bot may act now. For a paced persona this is also the signal that it must. */
    boolean due(long nowMillis) {
        return intervalMillis == 0L || nowMillis - lastCastAtMillis >= intervalMillis;
    }

    /** Whether this persona is paced at all. */
    boolean paced() {
        return intervalMillis != 0L;
    }

    void recordCast(long nowMillis) {
        lastCastAtMillis = nowMillis;
    }
}
