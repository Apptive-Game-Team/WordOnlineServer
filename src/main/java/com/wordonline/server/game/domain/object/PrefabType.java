package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.object.component.DummyComponent;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.effect.LeafFieldEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.Drop;
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
        gameObject.getComponents().add(new Shot(gameObject.getMaster(), gameObject, 10));
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
    FireSlime((gameObject -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Slime(gameObject, 19, 1, 10));
    })),

    Dummy((gameObject) -> {
        gameObject.getComponents().add(new DummyComponent(gameObject));
    }),

    FireField((gameObject) -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
    }),
    LeafField((gameObject) -> {
        gameObject.setRadius(0.5f);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new LeafFieldEffectReceiver(gameObject));
    }),;

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
