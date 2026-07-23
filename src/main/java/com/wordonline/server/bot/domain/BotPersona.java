package com.wordonline.server.bot.domain;

public record BotPersona(
        long userId,
        String name,
        BotTier tier,
        int thinkingTimeMs,
        int reactionIntervalFrames,
        double counterAggression,
        boolean enabled
) {
    public static final BotPersona DEFAULT = new BotPersona(
            0,
            "Default Bot",
            BotTier.BEGINNER,
            250,
            8,
            0.25,
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
