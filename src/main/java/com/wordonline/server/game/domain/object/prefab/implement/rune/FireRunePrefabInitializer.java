package com.wordonline.server.game.domain.object.prefab.implement.rune;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("fire_rune_prefab")
public class FireRunePrefabInitializer extends PrefabInitializer {

    public FireRunePrefabInitializer() {
        super(PrefabType.FireRune);
    }

    @Override
    public void initialize(GameObject gameObject) {

    }
}
