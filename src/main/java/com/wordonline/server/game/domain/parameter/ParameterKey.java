package com.wordonline.server.game.domain.parameter;

public enum ParameterKey {
    ATTACK_INTERVAL("attack_interval"),
    ATTACK_RANGE("attack_range"),
    CHAIN_COUNT("chain_count"),
    CHAIN_DAMAGE("chain_damage"),
    CHAIN_RADIUS("chain_radius"),
    DAMAGE("damage"),
    DETECTION_RANGE("detection_range"),
    DURATION("duration"),
    FEVER_DURATION("fever_duration"),
    HEAL_AMOUNT("heal_amount"),
    HEAL_INTERVAL("heal_interval"),
    HP("hp"),
    MASS("mass"),
    MAX_MANA("max_mana"),
    PANIC_DURATION("panic_duration"),
    QUANTITY("quantity"),
    RADIUS("radius"),
    RANGE("range"),
    Z_FORCE("z_force"),
    SPEED("speed");

    private final String dbName;

    ParameterKey(String dbName) {
        this.dbName = dbName;
    }

    public String dbName() {
        return dbName;
    }
}
