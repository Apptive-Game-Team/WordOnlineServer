package com.wordonline.server.bot.dto;

import com.wordonline.server.bot.domain.BotTier;

public record BotPersonaRequestDto(
        String name,
        BotTier tier,
        long deckId,
        int thinkingTimeMs,
        int reactionIntervalFrames,
        double counterAggression,
        short mmr,
        Boolean enabled
) {
}
