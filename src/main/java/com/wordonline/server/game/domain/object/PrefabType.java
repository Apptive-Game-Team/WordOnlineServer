package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.object.component.DummyComponent;

public enum PrefabType {
    Slime(null),
    Magic(null),
    Dummy((gameObject) -> {
        gameObject.getComponents().add(new DummyComponent(gameObject));
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
