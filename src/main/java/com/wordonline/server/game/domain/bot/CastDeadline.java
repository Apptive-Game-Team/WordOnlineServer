package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotTier;

/**
 * The longest the hospitality bot may go without casting.
 *
 * <p>This is a ceiling on silence, not a floor on frequency. Difficulty for the tutorial opponent
 * comes from one place only - it summons units the player's board beats - and never from casting
 * less often. But a bot that stands still reads as one that is going easy on the player, and that
 * costs more than losing the fight does, so once this long has passed without a cast the bot stops
 * holding out for a better moment and plays whatever it can.
 *
 * <p>Every other tier has no deadline: they are already trying to win, so they never go quiet for a
 * reason the player would misread.
 */
final class CastDeadline {

    /**
     * How long the hospitality bot may stay silent before it has to act.
     *
     * <p>A minute rather than half of one: the deadline exists so a quiet bot does not read as one
     * going easy, and a new player reading their cards does not notice half a minute passing. It
     * is not a difficulty knob - shortening it does not make the bot harder, it only makes it act
     * when it would otherwise have nothing worth doing.
     */
    static final long HOSPITALITY_MAX_SILENCE_MILLIS = 60_000L;

    /** Zero means the persona has no deadline at all. */
    private final long maxSilenceMillis;

    // Written by the bot executor thread when a cast goes out, read by the same thread when it
    // decides whether the bot is overdue. Volatile because the loop thread may also observe it.
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
