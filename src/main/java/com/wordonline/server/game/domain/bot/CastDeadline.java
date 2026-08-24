package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotTier;

/**
 * The longest the hospitality bot may go without casting.
 *
 * <p>A ceiling on silence, not a floor on frequency. The tutorial opponent is kept weak by what it
 * is allowed to summon - never anything that would put its board ahead of the player's - so it does
 * not also need to be slowed down. What it must not do is go quiet: a still opponent reads as one
 * going easy on the player, and that costs more than losing the fight does.
 *
 * <p>Every other tier has no deadline. They are already trying to win, so they never fall silent
 * for a reason the player would misread.
 */
final class CastDeadline {

    /** How long the hospitality bot may stay silent before it has to act. */
    static final long HOSPITALITY_MAX_SILENCE_MILLIS = 60_000L;

    /** Zero means the persona has no deadline at all. */
    private final long maxSilenceMillis;

    // Written by the bot executor thread when a cast goes out, read by the loop thread when it
    // decides whether the bot is overdue. Only one tick runs at a time, so a plain volatile is enough.
    private volatile long lastCastAtMillis;

    private CastDeadline(long maxSilenceMillis, long startedAtMillis) {
        this.maxSilenceMillis = maxSilenceMillis;
        this.lastCastAtMillis = startedAtMillis;
    }

    static CastDeadline forTier(BotTier tier, long startedAtMillis) {
        return new CastDeadline(
                tier == BotTier.HOSPITALITY ? HOSPITALITY_MAX_SILENCE_MILLIS : 0L,
                startedAtMillis);
    }

    /** Whether the bot has been quiet long enough that it must act now. */
    boolean overdue(long nowMillis) {
        return maxSilenceMillis != 0L && nowMillis - lastCastAtMillis >= maxSilenceMillis;
    }

    void recordCast(long nowMillis) {
        lastCastAtMillis = nowMillis;
    }
}
