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
    FALL_GRAVITY("fall_gravity"),
    HEAL_AMOUNT("heal_amount"),
    HEAL_INTERVAL("heal_interval"),
    HP("hp"),
    MASS("mass"),
    MAX_MANA("max_mana"),
    MIN_DAMAGE("min_damage"),
    PROJECTILE_SPEED("projectile_speed"),
    PUSH_FORCE("push_force"),
    PUSH_RANGE_X("push_range_x"),
    PUSH_RANGE_Y("push_range_y"),
    PANIC_DURATION("panic_duration"),
    QUANTITY("quantity"),
    RADIUS("radius"),
    RANGE("range"),
    SPAWN_HEIGHT("spawn_height"),
    VINE_COUNT("vine_count"),
    VINE_SPACING("vine_spacing"),
    VINE_SPAWN_INTERVAL("vine_spawn_interval"),
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
