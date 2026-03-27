package com.wordonline.server.game.domain.object.prefab;

import lombok.Getter;

@Getter
public enum PrefabType {

    // fire ========================================================
    FireShot("fire_shot_prefab"),
    FireExplode("fire_explode_prefab"),
    FireField("fire_field_prefab"),
    FireSlime("fire_slime_prefab"),
    FireSummon("fire_summon_prefab"),

    // water ========================================================
    WaterShot("water_shot_prefab"),
    WaterExplode("water_explode_prefab"),
    WaterField("water_field_prefab"),
    WaterSlime("water_slime_prefab"),

    // rock ========================================================
    RockRolling("rock_rolling_prefab"),
    RockExplode("rock_explode_prefab"),
    RockSlime("rock_slime_prefab"),
    RockSummon("rock_summon_prefab"),

    // electric ========================================================
    ElectricShot("electric_shot_prefab"),
    ElectricExplode("electric_explode_prefab"),
    ElectricSlime("electric_slime_prefab"),
    ElectricSummon("electric_summon_prefab"),
    ElectricField("electric_field_prefab"),

    // leaf ========================================================
    VineToss("vine_toss_prefab"),
    LeafExplode("leaf_explode_prefab"),
    LeafField("leaf_field_prefab"),
    LeafSlime("leaf_slime_prefab"),

    // wind ========================================================
    WindBlade("wind_blade_prefab"),
    WindExplode("wind_explode_prefab"),
    WindSlime("wind_slime_prefab"),
    WindSummon("wind_summon_prefab"),

    // 상위 마법 ========================================================
    GroundCannon("ground_cannon_prefab"),
    GroundTower("ground_tower_prefab"),
    ManaWell("mana_well_prefab"),
    AquaArcher("aqua_archer_prefab"),
    RockGolem("rock_golem_prefab"),
    StormRider("storm_rider_prefab"),
    FireSpirit("fire_spirit_prefab"),
    ThunderSpirit("thunder_spirit_prefab"),
    MagmaSpirit("magma_spirit_prefab"),
    HealingTotem("healing_totem_prefab"),
    SandStorm("sand_storm_prefab"),
    ChainLightning("chain_lightning_prefab"),
    TornadoStrike("tornado_strike_prefab"),
    MeteorShower("meteor_shower_prefab"),
    TideCall("tide_call_prefab"),

    // drop
    FireDrop("fire_drop_prefab"),
    WaterDrop("water_drop_prefab"),
    NatureDrop("nature_drop_prefab"),
    RockDrop("rock_drop_prefab"),
    LightningDrop("lightning_drop_prefab"),
    WindDrop("wind_drop_prefab"),

    // rune
    FireRune("fire_rune_prefab"),
    WaterRune("water_rune_prefab"),
    NatureRune("nature_rune_prefab"),
    RockRune("rock_rune_prefab"),
    LightningRune("lightning_rune_prefab"),
    WindRune("wind_rune_prefab"),

    // 3차 상위 마법
    TreeGolem("tree_golem_prefab"),
    VineSpirit("vine_spirit_prefab"),
    ThunderBird("thunder_bird_prefab"),
    CloudDragon("cloud_dragon_prefab"),

    // 베타전 마지막 상위 마법
    RockMage("rock_mage_prefab"),
    VineColony("vine_colony_prefab"),

    // 부수적 생산 프리팹
    Vine("vine_prefab"),
    MagmaFist("magma_fist_prefab"),
    MeteorDrop("meteor_drop_prefab"),

    // PVE
    PveNatureSlimeNest("pve_nature_slime_nest_prefab"),
    PveWaterSlimeNest("pve_water_slime_nest_prefab"),
    PveVineColony("pve_vine_colony_prefab"),
    PveVineWitch("pve_vine_witch_prefab"),

    // New 2 card Magic
    EmberSpirit("fire_slime_prefab"),
    SeedSpirit("seed_spirit_prefab"),
    WindSpirit("wind_spirit_prefab"),
    MiniRock("mini_rock_prefab"),

    LifeTree("life_tree_prefab"),
    RockTurret("rock_turret_prefab"),
    WindTotem("wind_totem_prefab"),

    MagmaExplosion("magma_explosion_prefab"),
    WaterExplosion("water_explosion_prefab"),



    Wall("wall_prefab"),

    Player("player_prefab");

    private final String beanName;

    PrefabType(String beanName) {
        this.beanName = beanName;
    }
}