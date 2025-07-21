package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.component.DummyComponent;
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
    FireShot((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    FireExplode((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    FireField((gameObject) -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, 5f));
    }),
    FireSlime((gameObject -> {
        gameObject.getComponents().add(new RigidBody(gameObject, 1));
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, false));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Slime(gameObject, 8, 0.8f, 3));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.FireField, 1f));
        gameObject.getComponents().add(new FireEffectReceiver(gameObject));
    })),
    FireSummon((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new Spawner(gameObject, 5, PrefabType.FireSlime));
    })),

    // water
    WaterShot((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    WaterExplode((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    WaterField((gameObject) -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, 5f));
    }),
    WaterSlime((gameObject -> {
        gameObject.getComponents().add(new RigidBody(gameObject, 1));
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, false));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Slime(gameObject, 8, 0.8f, 3));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.WaterField, 1f));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),
    WaterSummon((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.WATER);
        gameObject.getComponents().add(new Spawner(gameObject, 5, PrefabType.WaterSlime));
    })),

    // rock
    RockShot((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    RockExplode((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    RockSlime((gameObject -> {
        gameObject.getComponents().add(new RigidBody(gameObject, 1));
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, false));
        gameObject.getComponents().add(new Slime(gameObject, 8, 0.8f, 3));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),
    RockSummon((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Spawner(gameObject, 5, PrefabType.RockSlime));
    })),

    // electric
    ElectricShot((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    ElectricExplode((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    ElectricSlime((gameObject -> {
        gameObject.getComponents().add(new RigidBody(gameObject, 1));
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, false));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Slime(gameObject, 8, 0.8f, 3));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),
    ElectricSummon((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.LIGHTING);
        gameObject.getComponents().add(new Spawner(gameObject, 5, ElectricSlime));
    })),

    // leaf
    LeafShot((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    LeafExplode((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    LeafField((gameObject) -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new LeafFieldEffectReceiver(gameObject));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, 5f));
    }),
    LeafSlime((gameObject -> {
        gameObject.getComponents().add(new RigidBody(gameObject, 1));
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, false));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Slime(gameObject, 8, 0.8f, 3));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.LeafField, 1f));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),
    LeafSummon((gameObject -> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 0.5f, true));
        gameObject.setElement(ElementType.LEAF);
        gameObject.getComponents().add(new Spawner(gameObject, 5, PrefabType.LeafSlime));
    })),

    Dummy((gameObject) -> {
        gameObject.setElement(ElementType.NONE);
        gameObject.getComponents().add(new DummyComponent(gameObject));
    }),

    Wall((gameObject)-> {
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(0, 0), new Vector2(0, GameConfig.HEIGHT), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(GameConfig.WIDTH, GameConfig.HEIGHT), new Vector2(0, GameConfig.HEIGHT), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(GameConfig.WIDTH, GameConfig.HEIGHT), new Vector2(GameConfig.WIDTH, 0), false));
        gameObject.getColliders().add(new EdgeCollider(gameObject, new Vector2(0, 0), new Vector2(GameConfig.WIDTH, 0), false));
        gameObject.setElement(ElementType.NONE);
    }),

    Player((gameObject)-> {
        gameObject.getColliders().add(new CircleCollider(gameObject, 1, false));
        gameObject.setElement(ElementType.NONE);
        gameObject.getComponents().add(new PlayerHealthComponent(gameObject));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    });

    private final PrefabInitializer prefabInitializer;

    public void initialize(GameObject gameObject) {
        if (prefabInitializer != null) {
            prefabInitializer.initialize(gameObject);
        }
    }

    PrefabType(PrefabInitializer prefabInitializer) {
        this.prefabInitializer = prefabInitializer;
    }
}
