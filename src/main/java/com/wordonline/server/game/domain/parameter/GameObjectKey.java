package com.wordonline.server.game.domain.parameter;

public enum GameObjectKey {
    AQUA_ARCHER("aqua_archer"),
    BUBBLE_GENERATOR("bubble_generator"),
    BUBBLE_SPIRIT("bubble_spirit"),
    BUILD("build"),
    CHAIN_LIGHTNING("chain_lightning"),
    CHICKEN_COMMANDO("chicken_commando"),
    CLOUD_DRAGON("cloud_dragon"),
    CRATER("crater"),
    CRATER_EMBER("crater_ember"),
    DIMENSION_TOAD("dimension_toad"),
    DROP("drop"),
    ELECTRIC_TOWER("electric_tower"),
    EXPLODE("explode"),
    FIELD("field"),
    FIELD_SHORT("field_short"),
    FIRE_CHILD_SPIRIT("fire_child_spirit"),
    FIRE_LORD_SPIRIT("fire_lord_spirit"),
    FIRE_SPIRIT("fire_spirit"),
    FIRE_TADPOLE("fire_tadpole"),
    GAME("game"),
    GROUND_CANNON("ground_cannon"),
    GROUND_TOWER("ground_tower"),
    HEALING_TOTEM("healing_totem"),
    LIFE_TREE("life_tree"),
    LIGHTNING_TADPOLE("lightning_tadpole"),
    MAGMA_FIST("magma_fist"),
    MAGMA_SPIRIT("magma_spirit"),
    MANA_WELL("mana_well"),
    METEOR_DROP("meteor_drop"),
    METEOR_SHOWER("meteor_shower"),
    MINI_ROCK("mini_rock"),
    OVERGROWTH("overgrowth"),
    PLAYER("player"),
    RALLYING_TORCH("rallying_torch"),
    RAZOR_GALE("razor_gale"),
    ROCK_GOLEM("rock_golem"),
    ROCK_MAGE("rock_mage"),
    ROCK_TURRET("rock_turret"),
    RUNE("rune"),
    SAND_STORM("sand_storm"),
    SHOOT("shoot"),
    SLIME("slime"),
    STORM_RIDER("storm_rider"),
    THUNDER_BIRD("thunder_bird"),
    THUNDER_SPIRIT("thunder_spirit"),
    TIDE_CALL("tide_call"),
    VINE_TOSS("vine_toss"),
    TORNADO_STRIKE("tornado_strike"),
    TOWERBACK("towerback"),
    TREE_GOLEM("tree_golem"),
    VINE("vine"),
    VINE_COLONY("vine_colony"),
    VINE_SPIRIT("vine_spirit"),
    WATER_EXPLOSION("water_explosion"),
    WIND_SHOOT("wind_shoot"),
    WIND_SPIRIT("wind_spirit"),
    WIND_TOTEM("wind_totem"),
    ZAP_MOUSE("zap_mouse");

    private final String dbName;

    GameObjectKey(String dbName) {
        this.dbName = dbName;
    }

    public String dbName() {
        return dbName;
    }
}
