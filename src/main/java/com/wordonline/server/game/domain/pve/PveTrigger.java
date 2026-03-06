package com.wordonline.server.game.domain.pve;

public record PveTrigger(
        String id,
        PveTriggerType type,
        int value,
        PveDialogue dialogue
) {
}
