package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.effect.FireEffectReceiver;
import com.wordonline.server.game.domain.object.component.effect.LeafFieldEffectReceiver;
import com.wordonline.server.game.domain.object.component.PlayerHealthComponent;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.mob.statemachine.slime.Slime;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.EdgeCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.dto.Effect;

public enum PrefabType {

    // fire
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
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mess")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Slime(gameObject, (int) parameters.getValue("slime", "hp"), (float) parameters.getValue("slime", "speed"), (int) parameters.getValue("slime", "damage")));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.FireField, 1f));
        gameObject.getComponents().add(new FireEffectReceiver(gameObject));
    }),
    FireSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("summon", "radius"), true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("summon", "hp"), PrefabType.FireSlime));
    }),

    // water
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
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field", "duration")));
    }),
    WaterSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mess")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Slime(gameObject, (int) parameters.getValue("slime", "hp"), (float) parameters.getValue("slime", "speed"), (int) parameters.getValue("slime", "damage")));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.WaterField, 1f));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    WaterSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("summon", "radius"), true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("summon", "hp"), PrefabType.WaterSlime));
    }),

    // rock
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
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mess")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new Slime(gameObject, (int) parameters.getValue("slime", "hp"), (float) parameters.getValue("slime", "speed"), (int) parameters.getValue("slime", "damage")));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    RockSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("summon", "radius"), true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("summon", "hp"), PrefabType.RockSlime));
    }),

    // electric
    ElectricShot((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }),
    ElectricExplode((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }),
    ElectricSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mess")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Slime(gameObject, (int) parameters.getValue("slime", "hp"), (float) parameters.getValue("slime", "speed"), (int) parameters.getValue("slime", "damage")));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    ElectricSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("summon", "radius"), true));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("summon", "hp"), ElectricSlime));
    }),

    // leaf
    LeafShot((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }),
    LeafExplode((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }),
    LeafField((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("field", "radius"), true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new LeafFieldEffectReceiver(gameObject));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field", "duration")));
    }),
    LeafSlime((gameObject, parameters)  -> {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("slime", "mess")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("slime", "radius"), false));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Slime(gameObject, (int) parameters.getValue("slime", "hp"), (float) parameters.getValue("slime", "speed"), (int) parameters.getValue("slime", "damage")));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.LeafField, 1f));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }),
    LeafSummon((gameObject, parameters)  -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("summon", "radius"), true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new Spawner(gameObject, (int) parameters.getValue("summon", "hp"), PrefabType.LeafSlime));
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
