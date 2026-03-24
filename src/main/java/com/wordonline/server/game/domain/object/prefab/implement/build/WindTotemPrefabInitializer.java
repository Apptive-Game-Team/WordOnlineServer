package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("wind_totem_prefab")
public class WindTotemPrefabInitializer extends PrefabInitializer {

    public WindTotemPrefabInitializer() {
        super(PrefabType.WindTotem);
    }

    @Override
    public void initialize(GameObject gameObject) {

    }
}
