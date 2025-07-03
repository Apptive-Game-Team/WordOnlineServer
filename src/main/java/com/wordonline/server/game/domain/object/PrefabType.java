package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.object.component.DummyComponent;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.effect.LeafFieldEffectReceiver;
import com.wordonline.server.game.domain.object.component.PlayerHealthComponent;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.mob.statemachine.slime.Slime;
import com.wordonline.server.game.dto.Effect;

public enum PrefabType {
    // fire
    FireShot((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    FireSummon((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Spawner(gameObject, 5));
    })),
    FireExplode((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    FireField((gameObject) -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, 5f));
    }),
    FireSlime((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Slime(gameObject, 19, 1, 10));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.FireField, 1f));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),

    // water
    WaterShot((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    WaterSummon((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Spawner(gameObject, 5));
    })),
    WaterExplode((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    WaterField((gameObject) -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, 5f));
    }),
    WaterSlime((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Wet));
        gameObject.getComponents().add(new Slime(gameObject, 19, 1, 10));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.WaterField, 1f));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),

    // rock
    RockShot((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    RockSummon((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new Spawner(gameObject, 5));
    })),
    RockExplode((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    RockSlime((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new Slime(gameObject, 19, 1, 10));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),

    // electric
    ElectricShot((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    ElectricSummon((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Spawner(gameObject, 5));
    })),
    ElectricExplode((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    ElectricSlime((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new Slime(gameObject, 19, 1, 10));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),

    // leaf
    LeafShot((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Shot(gameObject, 10));
    })),
    LeafSummon((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Spawner(gameObject, 5));
    })),
    LeafExplode((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    LeafField((gameObject) -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new LeafFieldEffectReceiver(gameObject));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, 5f));
    }),
    LeafSlime((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Slime(gameObject, 19, 1, 10));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.LeafField, 1f));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    })),

    Dummy((gameObject) -> {
        gameObject.getComponents().add(new DummyComponent(gameObject));
    }),

    Player((gameObject)-> {
        gameObject.setRadius(1);
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
