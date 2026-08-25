package com.wordonline.server.bot.dto;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;

public record BotPersonaResponseDto(
        long userId,
        String name,
        BotTier tier,
        int thinkingTimeMs,
        int reactionIntervalFrames,
        double counterAggression,
        boolean enabled,
        boolean hospitality
) {
    public BotPersonaResponseDto(BotPersona persona) {
        this(
                persona.userId(),
                persona.name(),
                persona.tier(),
                persona.thinkingTimeMs(),
                persona.reactionIntervalFrames(),
                persona.counterAggression(),
                persona.enabled(),
                persona.hospitality()
        );
    }
}
