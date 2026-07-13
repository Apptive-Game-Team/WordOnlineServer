package com.wordonline.server.game.dto.lockstep;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LockstepInputType {
    USE_MAGIC("useMagic");

    private final String value;

    LockstepInputType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static LockstepInputType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown lockstep input type: " + value));
    }
}
