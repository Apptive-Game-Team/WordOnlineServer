package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.*;
import com.wordonline.server.game.domain.object.component.PlayerHealthComponent;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.effect.receiver.LeafFieldEffectReceiver;
import com.wordonline.server.game.domain.object.component.effect.receiver.WaterFieldEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.mob.Cannon;
import com.wordonline.server.game.domain.object.component.mob.ManaWellMob;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.MeleeAttackMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.RangeAttackMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.Slime;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.SummonerMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.EdgeCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Effect;

import java.util.EnumSet;

public enum PrefabType {

    // fire ========================================================
    FireShot((gameObject, parameters) -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }),
    FireExplode((gameObject, parameters) -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }),
    FireField((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("field", "radius"), true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field", "duration")));
    }),
    FireSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new Slime(gameObject,
                (int) parameters.getValue("slime", "hp"),
                (float) parameters.getValue("slime", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("slime", "damage"),
                (float) parameters.getValue("slime", "attack_interval")));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.FireField, 1f));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    FireSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), PrefabType.FireSlime));
    }),

    // water ========================================================
    WaterShot((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }),
    WaterExplode((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }),
    WaterField((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("field", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new WaterFieldEffectReceiver(gameObject));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field", "duration")));
    }),
    WaterSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new Slime(gameObject,
                (int) parameters.getValue("slime", "hp"),
                (float) parameters.getValue("slime", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("slime", "damage"),
                (float) parameters.getValue("slime", "attack_interval")));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.WaterField, 1f));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    WaterSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), PrefabType.WaterSlime));
    }),

    // rock ========================================================
    RockShot((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }),
    RockExplode((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }),
    RockSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new Slime(gameObject,
                (int) parameters.getValue("slime", "hp"),
                (float) parameters.getValue("slime", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("slime", "damage"),
                (float) parameters.getValue("slime", "attack_interval")));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    RockSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), PrefabType.RockSlime));
    }),

    // electric ========================================================
    ElectricShot((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }),
    ElectricExplode((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }),
    ElectricSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new Slime(gameObject,
                (int) parameters.getValue("slime", "hp"),
                (float) parameters.getValue("slime", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("slime", "damage"),
                (float) parameters.getValue("slime", "attack_interval")));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    ElectricSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), ElectricSlime));
    }),
    ElectricField((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("field_short", "radius"), true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field_short", "duration")));
    }),
    // leaf ========================================================
    LeafShot((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }),
    LeafExplode((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }),
    LeafField((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("field", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.LeafFieldHeal));
        gameObject.getComponents().add(new LeafFieldEffectReceiver(gameObject));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field", "duration")));
    }),
    LeafSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new Slime(gameObject,
                (int) parameters.getValue("slime", "hp"),
                (float) parameters.getValue("slime", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("slime", "damage"),
                (float) parameters.getValue("slime", "attack_interval")));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.LeafField, 1f));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    LeafSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), PrefabType.LeafSlime));
    }),

    // wind ========================================================
    WindShot((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new KnockbackEffectProvider(gameObject, Effect.Knockback));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("wind_shoot", "damage")));
    }),
    WindExplode((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new KnockbackEffectProvider(gameObject, Effect.Knockback));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("wind_shoot", "damage")));
    }),
    WindSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new Slime(gameObject,
                (int) parameters.getValue("slime", "hp"),
                (float) parameters.getValue("slime", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("slime", "damage"),
                (float) parameters.getValue("slime", "attack_interval")));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    WindSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("build", "radius"), true));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("build", "hp"), PrefabType.WindSlime));
    }),

    // 상위 마법 ========================================================
    GroundCannon((gameObject, parameters) -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("ground_cannon", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("ground_cannon", "radius"), false));
        gameObject.getComponents().add(new Cannon(gameObject, (int) parameters.getValue("ground_cannon", "hp"), (int) parameters.getValue("ground_cannon", "damage"), TargetMask.GROUND.bit));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    GroundTower((gameObject, parameters) -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("ground_tower", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("ground_tower", "radius"), false));
        gameObject.getComponents().add(new Cannon(gameObject, (int) parameters.getValue("ground_tower", "hp"), (int) parameters.getValue("ground_tower", "damage"), TargetMask.AIR.bit));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    ManaWell((gameObject, parameters) -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("mana_well", "radius"), false));
        gameObject.getComponents().add(new ManaWellMob(gameObject,
                (int) parameters.getValue("mana_well", "hp")
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING, ElementType.NATURE));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    AquaArcher((gameObject, parameters) -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("aqua_archer", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("aqua_archer", "radius"), false));
        gameObject.getComponents().add(new RangeAttackMob(gameObject,
                (int) parameters.getValue("aqua_archer", "hp"),
                (float) parameters.getValue("aqua_archer", "speed"),
                TargetMask.ANY.bit,
                (int) parameters.getValue("aqua_archer", "damage"),
                (float) parameters.getValue("aqua_archer", "attack_interval"),
                (float) parameters.getValue("aqua_archer", "attack_range")
        ));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    RockGolem((gameObject, parameters) -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("rock_golem", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("rock_golem", "radius"), false));
        gameObject.getComponents().add(new MeleeAttackMob(gameObject,
                (int) parameters.getValue("rock_golem", "hp"),
                (float) parameters.getValue("rock_golem", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("rock_golem", "damage"),
                (float) parameters.getValue("rock_golem", "attack_interval")
        ));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    StormRider((gameObject, parameters) -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("storm_rider", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("storm_rider", "radius"), false));
        gameObject.getComponents().add(new MeleeAttackMob(gameObject,
                (int) parameters.getValue("storm_rider", "hp"),
                (float) parameters.getValue("storm_rider", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("storm_rider", "damage"),
                (float) parameters.getValue("storm_rider", "attack_interval")
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING,ElementType.WATER));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    FireSpirit((gameObject, parameters) -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("fire_spirit", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("fire_spirit", "radius"), false));
        gameObject.getComponents().add(new SummonerMob(gameObject,
                (int) parameters.getValue("fire_spirit", "hp"),
                (float) parameters.getValue("fire_spirit", "speed"),
                TargetMask.GROUND.bit,
                (float) parameters.getValue("fire_spirit", "attack_interval"),
                (float) parameters.getValue("fire_spirit", "attack_range"),
                PrefabType.FireField
        ));
        gameObject.setElement(EnumSet.of(ElementType.FIRE,ElementType.WIND));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    ThunderSpirit((gameObject, parameters) -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("thunder_spirit", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("thunder_spirit", "radius"), false));
        gameObject.getComponents().add(new RangeAttackMob(gameObject,
                (int) parameters.getValue("thunder_spirit", "hp"),
                (float) parameters.getValue("thunder_spirit", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("thunder_spirit", "damage"),
                (float) parameters.getValue("thunder_spirit", "attack_interval"),
                (float) parameters.getValue("thunder_spirit", "attack_range")
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING,ElementType.WIND));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),

    Wall((gameObject, parameters) -> {
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(0, 0), new Vector2(0, GameConfig.HEIGHT), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(GameConfig.WIDTH, GameConfig.HEIGHT), new Vector2(0, GameConfig.HEIGHT), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(GameConfig.WIDTH, GameConfig.HEIGHT), new Vector2(GameConfig.WIDTH, 0), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(0, 0), new Vector2(GameConfig.WIDTH, 0), false));
        gameObject.setElement(ElementType.NONE);
    }),

    Player((gameObject, parameters) -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 1, false));
        gameObject.setElement(ElementType.NONE);
        gameObject.getComponents().add(new PlayerHealthComponent(gameObject));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    });

    private final PrefabInitializer prefabInitializer;

    public void initialize(GameObject gameObject, Parameters parameters) {
        if (prefabInitializer != null) {
            prefabInitializer.initialize(gameObject, parameters);
        }
    }

    PrefabType(PrefabInitializer prefabInitializer) {
        this.prefabInitializer = prefabInitializer;
    }
}
