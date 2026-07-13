package com.wordonline.server.game.dto.lockstep;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LockstepMessageType {
    SESSION_START("lockstepSessionStart"),
    CONFIRMED_FRAME("confirmedFrame"),
    ABORT("lockstepAbort");

    private final String value;

    LockstepMessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
