package com.wordonline.server.bot.domain;

public enum BotTier {
    /**
     * The tutorial opponent. Not a difficulty step below INTRO but a different job: it plays to
     * lose convincingly rather than to play badly.
     */
    HOSPITALITY,
    INTRO,
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ELITE
}
