package com.wordonline.server.game.dto.lockstep;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LockstepAbortReason {
    INPUT_TIMEOUT("input-timeout"),
    READY_TIMEOUT("ready-timeout"),
    VERSION_MISMATCH("version-mismatch"),
    PARTICIPANT_DISCONNECTED("participant-disconnected"),
    PEER_HASH_MISMATCH("peer-hash-mismatch"),
    RELAY_INTERRUPTED("relay-interrupted");

    private final String value;

    LockstepAbortReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
