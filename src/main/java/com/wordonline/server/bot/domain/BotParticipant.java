package com.wordonline.server.bot.domain;

public final class BotParticipant {

    private BotParticipant() {
    }

    public static boolean isBot(long participantId) {
        return participantId < 0;
    }

    public static long personaId(long participantId) {
        if (!isBot(participantId)) {
            throw new IllegalArgumentException("Participant is not a bot: " + participantId);
        }
        return -participantId;
    }

    public static long participantId(long personaId) {
        return -Math.abs(personaId);
    }
}
