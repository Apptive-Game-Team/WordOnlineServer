package com.wordonline.server.game.domain.parameter;

public enum ParameterKey {
    ATTACK_INTERVAL("attack_interval"),
    ATTACK_RANGE("attack_range"),
    BEAM_WIDTH("beam_width"),
    BUFF_DURATION("buff_duration"),
    CHAIN_COUNT("chain_count"),
    CHAIN_DAMAGE("chain_damage"),
    CHAIN_LIGHTNING_COOLDOWN("chain_lightning_cooldown"),
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
    SPAWN_INTERVAL("spawn_interval"),
    SUB_DAMAGE("sub_damage"),
    SUB_SPEED("sub_speed"),
    VINE_COUNT("vine_count"),
    VINE_SPACING("vine_spacing"),
    VINE_SPAWN_INTERVAL("vine_spawn_interval"),
    Z_FORCE("z_force"),
    SPEED("speed"),

    SUB_ATTACK_RANGE("sub_attack_range");

    private final String dbName;

    ParameterKey(String dbName) {
        this.dbName = dbName;
    }

    public String dbName() {
        return dbName;
    }
}
