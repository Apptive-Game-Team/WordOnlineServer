package com.wordonline.server.bot.domain;

public record BotPersona(
        long id,
        String name,
        BotTier tier,
        long deckId,
        int thinkingTimeMs,
        int reactionIntervalFrames,
        double counterAggression,
        short mmr,
        boolean enabled
) {
    public static final BotPersona DEFAULT = new BotPersona(
            0,
            "Default Bot",
            BotTier.BEGINNER,
            0,
            250,
            8,
            0.25,
            (short) 1000,
            true
    );

    public int normalizedReactionIntervalFrames() {
        return Math.max(1, reactionIntervalFrames);
    }

    public int normalizedThinkingTimeMs() {
        return Math.max(0, thinkingTimeMs);
    }

    public double normalizedCounterAggression() {
        return Math.max(0.0, Math.min(1.0, counterAggression));
    }
}
