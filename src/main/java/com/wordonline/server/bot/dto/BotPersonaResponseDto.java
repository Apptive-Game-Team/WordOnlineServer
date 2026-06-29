package com.wordonline.server.bot.dto;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;

public record BotPersonaResponseDto(
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
    public BotPersonaResponseDto(BotPersona persona) {
        this(
                persona.id(),
                persona.name(),
                persona.tier(),
                persona.deckId(),
                persona.thinkingTimeMs(),
                persona.reactionIntervalFrames(),
                persona.counterAggression(),
                persona.mmr(),
                persona.enabled()
        );
    }
}
