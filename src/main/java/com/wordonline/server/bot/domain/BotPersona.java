package com.wordonline.server.bot.domain;

public record BotPersona(
        long userId,
        String name,
        BotTier tier,
        int thinkingTimeMs,
        int reactionIntervalFrames,
        double counterAggression,
        boolean enabled,
        boolean hospitality
) {
    public static final BotPersona DEFAULT = new BotPersona(
            0,
            "Default Bot",
            BotTier.BEGINNER,
            250,
            8,
            0.25,
            true,
            false
    );

    public int normalizedReactionIntervalFrames() {
        return Math.max(1, reactionIntervalFrames);
    }

    public int normalizedThinkingTimeMs() {
        return Math.max(0, thinkingTimeMs);
    }

    /**
     * Negative values are meaningful, not a mistake to clamp away. The sign chooses which
     * direction of the matchup the bot is scoring: a positive persona prefers the play that
     * beats what is on the field, a negative one prefers the play that field answers best.
     * Only the hospitality bot uses the negative half - it has to lose to the units already
     * standing there while still putting a real unit down.
     */
    public double normalizedCounterAggression() {
        return Math.max(-1.0, Math.min(1.0, counterAggression));
    }
}
