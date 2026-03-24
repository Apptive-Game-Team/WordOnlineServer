package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("rock_turret_prefab")
public class RockTurretPrefabInitializer extends PrefabInitializer {

    public RockTurretPrefabInitializer() {
        super(PrefabType.RockTurret);
    }

    @Override
    public void initialize(GameObject gameObject) {

    }
}
