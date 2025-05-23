package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.object.component.DummyComponent;
import com.wordonline.server.game.domain.object.component.PlayerHealthComponent;
import com.wordonline.server.game.domain.object.component.magic.Drop;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.mob.statemachine.slime.Slime;

import static com.wordonline.server.game.domain.magic.CardType.Summon;

public enum PrefabType {
    // fire
    FireShot((gameObject -> {
        gameObject.setRadius(1);
        gameObject.getComponents().add(new Shot(gameObject.getMaster(), gameObject, 10));
    })),
    FireDrop((gameObject -> {
        gameObject.setRadius(1);
        gameObject.getComponents().add(new Drop(gameObject, 7));
    })),
    FireSummon((gameObject -> {
        gameObject.setRadius(1);
        gameObject.getComponents().add(new Spawner(gameObject, 5));
    })),
    FireExplode((gameObject -> {
        gameObject.setRadius(1);
        gameObject.getComponents().add(new Explode(gameObject, 8));
    })),
    FireSlime((gameObject -> {
        gameObject.setRadius(1);
        gameObject.getComponents().add(new Slime(gameObject, 19, 1, 10));
    })),

    Dummy((gameObject) -> {
        gameObject.getComponents().add(new DummyComponent(gameObject));
    }),

    Player((gameObject)-> {
        gameObject.setRadius(1);
        gameObject.getComponents().add(new PlayerHealthComponent(gameObject));
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
