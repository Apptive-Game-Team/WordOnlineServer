package com.wordonline.server.bot.domain;

public final class BotParticipant {

    private BotParticipant() {
    }

    public static boolean isBot(long participantId) {
        return participantId < 0;
    }

    public static void requireBotUserId(long userId) {
        if (!isBot(userId)) {
            throw new IllegalArgumentException("Bot user ID must be negative: " + userId);
        }
    }
}
