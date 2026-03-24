package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("life_tree_prefab")
public class LifeTreePrefabInitializer extends PrefabInitializer {

    public LifeTreePrefabInitializer() {
        super(PrefabType.LifeTree);
    }

    @Override
    public void initialize(GameObject gameObject) {

    }
}
